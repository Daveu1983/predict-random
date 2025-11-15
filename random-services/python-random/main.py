from flask import Flask, jsonify, request
import random

app = Flask(__name__)

@app.before_request
def limit_remote_addr():
    if request.headers.get('X-App-Name') != 'getting-random':
        return jsonify({"error": "Access denied"}), 403

@app.route('/random', methods=['GET'])
def get_random_number():
    random_number = random.randint(1, 100)
    return jsonify({"random_number": random_number})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)