import re
import os
import requests
import json
from datetime import datetime
from flask import Flask, request, jsonify, Response
from flask_cors import CORS
from dotenv import load_dotenv
from pymongo import MongoClient
from bson import ObjectId

load_dotenv()
app = Flask(__name__)
CORS(app)

# API setup
API_URL = "https://router.huggingface.co/novita/v3/openai/chat/completions"
HF_API_TOKEN = os.getenv('HF_API_TOKEN', 'YOUR_HUGGINGFACE_API_TOKEN')
PORT = os.getenv('PORT', 4000)
HEADERS = {"Authorization": f"Bearer {HF_API_TOKEN}"}
MODEL = "meta-llama/llama-4-scout-17b-16e-instruct"

# MongoDB setup
MONGO_URI = os.getenv('MONGO_URI', 'mongodb://localhost:27017')
DB_NAME = os.getenv('DB_NAME', 'learning_experience_app')

# Connect to MongoDB
client = MongoClient(MONGO_URI)
db = client[DB_NAME]

# MongoDB collections
users_collection = db['users']
quiz_history_collection = db['quiz_history']
purchases_collection = db['purchases']
tasks_collection = db['tasks']

# JSON encoder to handle ObjectId and datetime
class MongoJSONEncoder(json.JSONEncoder):
    def default(self, obj):
        if isinstance(obj, ObjectId):
            return str(obj)
        if isinstance(obj, datetime):
            return obj.isoformat()
        return super(MongoJSONEncoder, self).default(obj)

# Helper function to convert MongoDB documents to JSON
def mongo_to_json(data):
    return json.dumps(data, cls=MongoJSONEncoder)

def fetchQuizFromLlama(student_topic):
    print("Fetching quiz from Hugging Face router API")
    payload = {
        "messages": [
            {
                "role": "user",
                "content": (
                    f"Generate a quiz with 3 questions to test students on the provided topic. "
                    f"For each question, generate 4 options where only one of the options is correct. "
                    f"Format your response as follows:\n"
                    f"**QUESTION 1:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"**QUESTION 2:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"**QUESTION 3:** [Your question here]?\n"
                    f"**OPTION A:** [First option]\n"
                    f"**OPTION B:** [Second option]\n"
                    f"**OPTION C:** [Third option]\n"
                    f"**OPTION D:** [Fourth option]\n"
                    f"**ANS:** [Correct answer letter]\n\n"
                    f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
                    f"Follow this pattern for all questions. "
                    f"Here is the student topic:\n{student_topic}"
                )
            }
        ],
        "model": MODEL,
        "max_tokens": 500,
        "temperature": 0.7,
        "top_p": 0.9
    }

    response = requests.post(API_URL, headers=HEADERS, json=payload)
    if response.status_code == 200:
        result = response.json()["choices"][0]["message"]["content"]
        return result
    else:
        raise Exception(f"API request failed: {response.status_code} - {response.text}")

def process_quiz(quiz_text):
    questions = []
    # Updated regex to match bolded format with numbered questions
    pattern = re.compile(
        r'\*\*QUESTION \d+:\*\* (.+?)\n'
        r'\*\*OPTION A:\*\* (.+?)\n'
        r'\*\*OPTION B:\*\* (.+?)\n'
        r'\*\*OPTION C:\*\* (.+?)\n'
        r'\*\*OPTION D:\*\* (.+?)\n'
        r'\*\*ANS:\*\* (.+?)(?=\n|$)',
        re.DOTALL
    )
    matches = pattern.findall(quiz_text)

    for match in matches:
        question = match[0].strip()
        options = [match[1].strip(), match[2].strip(), match[3].strip(), match[4].strip()]
        correct_ans = match[5].strip()

        question_data = {
            "question": question,
            "options": options,
            "correct_answer": correct_ans
        }
        questions.append(question_data)

    return questions

@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if not student_topic:
        return jsonify({'error': 'Missing topic parameter'}), 400
    try:
        quiz = fetchQuizFromLlama(student_topic)
        print(quiz)
        processed_quiz = process_quiz(quiz)
        if not processed_quiz:
            return jsonify({'error': 'Failed to parse quiz data', 'raw_response': quiz}), 500
        return jsonify({'quiz': processed_quiz}), 200
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

# User Management Endpoints
@app.route('/api/users/register', methods=['POST'])
def register_user():
    try:
        data = request.json
        required_fields = ['username', 'password']
        
        # Validate required fields
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        # Check if username already exists
        existing_user = users_collection.find_one({'username': data['username']})
        if existing_user:
            return jsonify({'error': 'Username already exists'}), 400
        
        # Create user document
        user = {
            'username': data['username'],
            'password': data['password'],  # In a real app, this should be hashed
            'createdAt': datetime.utcnow(),
            'interests': []
        }
        
        # Insert into MongoDB
        result = users_collection.insert_one(user)
        
        # Get the inserted document
        inserted_doc = users_collection.find_one({'_id': result.inserted_id})
        inserted_doc['_id'] = str(inserted_doc['_id'])  # Convert ObjectId to string
        
        return jsonify(inserted_doc), 201
    except Exception as e:
        print(f"Error creating user: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/users/login', methods=['POST'])
def login_user():
    try:
        data = request.json
        required_fields = ['username', 'password']
        
        # Validate required fields
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        # Find user by username and password
        user = users_collection.find_one({
            'username': data['username'],
            'password': data['password']
        })
        
        if not user:
            return jsonify({'error': 'Invalid username or password'}), 401
        
        # Convert ObjectId to string for JSON serialization
        user['_id'] = str(user['_id'])
        
        return jsonify(user), 200
    except Exception as e:
        print(f"Error logging in: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/users/<user_id>', methods=['GET'])
def get_user(user_id):
    try:
        # Find the user
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Convert ObjectId to string for JSON serialization
        user['_id'] = str(user['_id'])
        
        return jsonify(user), 200
    except Exception as e:
        print(f"Error fetching user: {str(e)}")
        return jsonify({'error': str(e)}), 500

# User Interests Endpoints
@app.route('/api/users/<user_id>/interests', methods=['GET'])
def get_user_interests(user_id):
    try:
        # Find the user
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Return the interests array
        return jsonify({'interests': user.get('interests', [])}), 200
    except Exception as e:
        print(f"Error fetching user interests: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/users/<user_id>/interests', methods=['POST'])
def update_user_interests(user_id):
    try:
        data = request.json
        
        if 'interests' not in data or not isinstance(data['interests'], list):
            return jsonify({'error': 'Interests must be provided as an array'}), 400
        
        # Update the user's interests
        result = users_collection.update_one(
            {'_id': ObjectId(user_id)},
            {'$set': {'interests': data['interests']}}
        )
        
        if result.matched_count == 0:
            return jsonify({'error': 'User not found'}), 404
        
        return jsonify({'message': 'Interests updated successfully', 'interests': data['interests']}), 200
    except Exception as e:
        print(f"Error updating user interests: {str(e)}")
        return jsonify({'error': str(e)}), 500

# Quiz History Endpoints
@app.route('/api/quizhistory', methods=['POST'])
def create_quiz_history():
    try:
        data = request.json
        required_fields = ['userId', 'quizId', 'score', 'totalQuestions']
        
        # Validate required fields
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        # Create quiz history document
        quiz_history = {
            'userId': data['userId'],
            'quizId': data['quizId'],
            'score': data['score'],
            'totalQuestions': data['totalQuestions'],
            'completedAt': datetime.utcnow(),
            'isShared': data.get('isShared', False)
        }
        
        # Insert into MongoDB
        result = quiz_history_collection.insert_one(quiz_history)
        
        # Get the inserted document
        inserted_doc = quiz_history_collection.find_one({'_id': result.inserted_id})
        inserted_doc['_id'] = str(inserted_doc['_id'])  # Convert ObjectId to string
        
        return jsonify(inserted_doc), 201
    except Exception as e:
        print(f"Error creating quiz history: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/quizhistory/user/<user_id>', methods=['GET'])
def get_user_quiz_history(user_id):
    try:
        # Find all quiz history for the user
        history = list(quiz_history_collection.find({'userId': user_id}).sort('completedAt', -1))
        
        # Convert ObjectId to string for JSON serialization
        for item in history:
            item['_id'] = str(item['_id'])
        
        return Response(mongo_to_json(history), mimetype='application/json')
    except Exception as e:
        print(f"Error fetching quiz history: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/quizhistory/<history_id>', methods=['GET'])
def get_quiz_history(history_id):
    try:
        # Find the specific quiz history
        history = quiz_history_collection.find_one({'_id': ObjectId(history_id)})
        
        if not history:
            return jsonify({'error': 'Quiz history not found'}), 404
        
        # Convert ObjectId to string for JSON serialization
        history['_id'] = str(history['_id'])
        
        return jsonify(history), 200
    except Exception as e:
        print(f"Error fetching quiz history: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/quizhistory/<history_id>', methods=['PATCH'])
def update_quiz_history(history_id):
    try:
        data = request.json
        
        # Update the quiz history
        result = quiz_history_collection.update_one(
            {'_id': ObjectId(history_id)},
            {'$set': data}
        )
        
        if result.matched_count == 0:
            return jsonify({'error': 'Quiz history not found'}), 404
        
        # Get the updated document
        updated_doc = quiz_history_collection.find_one({'_id': ObjectId(history_id)})
        updated_doc['_id'] = str(updated_doc['_id'])  # Convert ObjectId to string
        
        return jsonify(updated_doc), 200
    except Exception as e:
        print(f"Error updating quiz history: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/quizhistory/shared/<user_id>', methods=['GET'])
def get_shared_quiz_history(user_id):
    try:
        # Find all shared quiz history for the user
        history = list(quiz_history_collection.find({'userId': user_id, 'isShared': True}).sort('completedAt', -1))
        
        # Convert ObjectId to string for JSON serialization
        for item in history:
            item['_id'] = str(item['_id'])
        
        return Response(mongo_to_json(history), mimetype='application/json')
    except Exception as e:
        print(f"Error fetching shared quiz history: {str(e)}")
        return jsonify({'error': str(e)}), 500

# Purchase Endpoints
@app.route('/api/purchases', methods=['POST'])
def create_purchase():
    try:
        data = request.json
        required_fields = ['userId', 'productId', 'productName', 'price', 'paymentMethod']
        
        # Validate required fields
        for field in required_fields:
            if field not in data:
                return jsonify({'error': f'Missing required field: {field}'}), 400
        
        # Create purchase document
        purchase = {
            'userId': data['userId'],
            'productId': data['productId'],
            'productName': data['productName'],
            'price': data['price'],
            'paymentMethod': data['paymentMethod'],
            'purchaseDate': datetime.utcnow(),
            'status': 'completed',
            'transactionId': data.get('transactionId', '')
        }
        
        # Insert into MongoDB
        result = purchases_collection.insert_one(purchase)
        
        # Get the inserted document
        inserted_doc = purchases_collection.find_one({'_id': result.inserted_id})
        inserted_doc['_id'] = str(inserted_doc['_id'])  # Convert ObjectId to string
        inserted_doc['userId'] = str(inserted_doc['userId'])  # Convert ObjectId to string
        
        # Generate tasks for the user based on their package type
        generate_tasks_for_user(data['userId'], data['packageType'])
        
        # Count the tasks created
        task_count = tasks_collection.count_documents({
            'userId': ObjectId(data['userId']),
            'completed': False
        })
        
        # Add task count to response
        inserted_doc['taskCount'] = task_count
        inserted_doc['message'] = f"Successfully generated {task_count} tasks based on your purchase"
        
        return jsonify(inserted_doc), 201
    except Exception as e:
        print(f"Error creating purchase: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/purchases/user/<user_id>', methods=['GET'])
def get_user_purchases(user_id):
    try:
        # Find all purchases for the user
        purchases = list(purchases_collection.find({'userId': user_id}).sort('purchaseDate', -1))
        
        # Convert ObjectId to string for JSON serialization
        for item in purchases:
            item['_id'] = str(item['_id'])
        
        return Response(mongo_to_json(purchases), mimetype='application/json')
    except Exception as e:
        print(f"Error fetching purchases: {str(e)}")
        return jsonify({'error': str(e)}), 500

# Task Management
def generate_tasks_for_user(user_id, package_type):
    try:
        # Get user interests to personalize tasks
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        if not user:
            print(f"User {user_id} not found when generating tasks")
            return
            
        user_interests = user.get('interests', [])
        if not user_interests:
            user_interests = ['general programming']  # Default if no interests set
        
        # Determine number and difficulty of tasks based on package type
        if package_type == 'starter':
            task_count = 5
            difficulty_levels = ['beginner']
        elif package_type == 'intermediate':
            task_count = 10
            difficulty_levels = ['beginner', 'intermediate']
        elif package_type == 'advanced':
            task_count = 15
            difficulty_levels = ['beginner', 'intermediate', 'advanced']
        else:
            task_count = 3
            difficulty_levels = ['beginner']
        
        # Generate tasks based on interests and package
        tasks = []
        for i in range(task_count):
            # Cycle through interests and difficulty levels
            interest = user_interests[i % len(user_interests)]
            difficulty = difficulty_levels[i % len(difficulty_levels)]
            
            # Create task with due date based on difficulty
            due_days = 3 if difficulty == 'beginner' else 5 if difficulty == 'intermediate' else 7
            due_date = datetime.utcnow().replace(hour=23, minute=59, second=59)
            due_date = due_date.replace(day=due_date.day + due_days)
            
            task = {
                'userId': ObjectId(user_id),
                'title': f"Learn {interest.capitalize()} - {difficulty.capitalize()} Level",
                'description': f"Complete a {difficulty} level tutorial on {interest}",
                'dueDate': due_date,
                'completed': False,
                'difficulty': difficulty,
                'createdAt': datetime.utcnow()
            }
            tasks.append(task)
        
        # Insert tasks into MongoDB
        if tasks:
            tasks_collection.insert_many(tasks)
            print(f"Generated {len(tasks)} tasks for user {user_id}")
    except Exception as e:
        print(f"Error generating tasks: {str(e)}")

@app.route('/api/users/<user_id>/generate-tasks', methods=['POST'])
def generate_tasks_endpoint(user_id):
    try:
        data = request.json
        package_type = data.get('packageType', 'starter')
        
        # Validate user exists
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Delete existing incomplete tasks if regenerating
        if data.get('regenerate', False):
            tasks_collection.delete_many({
                'userId': ObjectId(user_id),
                'completed': False
            })
        
        # Generate new tasks
        generate_tasks_for_user(user_id, package_type)
        
        # Count the tasks created
        task_count = tasks_collection.count_documents({
            'userId': ObjectId(user_id),
            'completed': False
        })
        
        return jsonify({
            'success': True,
            'taskCount': task_count,
            'message': f"Successfully generated {task_count} tasks for user"
        }), 201
    except Exception as e:
        print(f"Error in generate_tasks_endpoint: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/users/<user_id>/tasks', methods=['GET'])
def get_user_tasks(user_id):
    try:
        # Validate user exists
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Get all tasks for the user
        tasks = list(tasks_collection.find({'userId': ObjectId(user_id)}).sort('dueDate', 1))
        
        # Convert ObjectId to string for JSON serialization
        for task in tasks:
            task['_id'] = str(task['_id'])
            task['userId'] = str(task['userId'])
        
        return jsonify({'tasks': tasks}), 200
    except Exception as e:
        print(f"Error getting user tasks: {str(e)}")
        return jsonify({'error': str(e)}), 500

@app.route('/api/tasks/<task_id>/complete', methods=['POST'])
def complete_task(task_id):
    try:
        # Find and update the task
        result = tasks_collection.update_one(
            {'_id': ObjectId(task_id)},
            {'$set': {'completed': True, 'completedAt': datetime.utcnow()}}
        )
        
        if result.modified_count == 0:
            return jsonify({'error': 'Task not found or already completed'}), 404
        
        # Get the updated task
        task = tasks_collection.find_one({'_id': ObjectId(task_id)})
        task['_id'] = str(task['_id'])
        task['userId'] = str(task['userId'])
        
        return jsonify(task), 200
    except Exception as e:
        print(f"Error completing task: {str(e)}")
        return jsonify({'error': str(e)}), 500

# User Profile Sharing
@app.route('/api/users/<user_id>/share', methods=['GET'])
def get_shareable_profile(user_id):
    try:
        # Find the user
        user = users_collection.find_one({'_id': ObjectId(user_id)})
        
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Get shared quiz history
        history = list(quiz_history_collection.find({'userId': user_id, 'isShared': True}).sort('completedAt', -1))
        
        # Create shareable profile
        shareable_profile = {
            'userId': str(user['_id']),
            'username': user.get('username', ''),
            'totalQuizzes': len(history),
            'averageScore': sum([h['score'] / h['totalQuestions'] * 100 for h in history]) / len(history) if history else 0,
            'quizHistory': history
        }
        
        # Convert ObjectId to string for JSON serialization
        for item in shareable_profile['quizHistory']:
            item['_id'] = str(item['_id'])
        
        return Response(mongo_to_json(shareable_profile), mimetype='application/json')
    except Exception as e:
        print(f"Error creating shareable profile: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    port_num = int(PORT)
    print(f"App running on port {port_num}")
    app.run(port=port_num, host="0.0.0.0")
