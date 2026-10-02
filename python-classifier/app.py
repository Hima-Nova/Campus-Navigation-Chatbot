"""
Python REST API Server for Campus Navigation Intent Classification
Provides /classify and /health endpoints.
Works using Python's built-in http.server (zero dependencies required)
with optional Flask support if installed.
"""

import os
import sys
import json
from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse
from classifier import KeywordIntentClassifier

PORT = int(os.environ.get('PORT', 5000))
classifier = KeywordIntentClassifier()

class IntentRequestHandler(BaseHTTPRequestHandler):
    protocol_version = 'HTTP/1.1'

    def _send_cors_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type, Authorization')
        self.send_header('Connection', 'close')

    def do_OPTIONS(self):
        self.send_response(204)
        self._send_cors_headers()
        self.end_headers()

    def do_GET(self):
        parsed = urlparse(self.path)
        if parsed.path in ('/health', '/', '/api/health'):
            response_data = {
                "status": "UP",
                "service": "campus-navigation-python-intent-classifier",
                "version": "1.0.0",
                "supportedIntents": ["DIRECTIONS", "TIMING", "GENERAL", "LOCATION_SEARCH", "UNKNOWN"]
            }
            body = json.dumps(response_data).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self._send_cors_headers()
            self.end_headers()
            self.wfile.write(body)
        else:
            body = json.dumps({"error": "Not Found"}).encode('utf-8')
            self.send_response(404)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self._send_cors_headers()
            self.end_headers()
            self.wfile.write(body)

    def do_POST(self):
        parsed = urlparse(self.path)
        if parsed.path in ('/classify', '/api/classify'):
            content_length = int(self.headers.get('Content-Length', 0))
            post_body = self.rfile.read(content_length) if content_length > 0 else b''
            
            try:
                data = json.loads(post_body.decode('utf-8')) if post_body else {}
                query = data.get('query', '')
                print(f"[Python-Classifier] Received query: '{query}' from body: {post_body.decode('utf-8', errors='ignore')}")
                
                result = classifier.classify(query)
                body = json.dumps(result).encode('utf-8')

                self.send_response(200)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', str(len(body)))
                self._send_cors_headers()
                self.end_headers()
                self.wfile.write(body)
            except Exception as e:
                err_data = {
                    "intent": "UNKNOWN",
                    "error": str(e),
                    "status": "error"
                }
                body = json.dumps(err_data).encode('utf-8')
                self.send_response(400)
                self.send_header('Content-Type', 'application/json')
                self.send_header('Content-Length', str(len(body)))
                self._send_cors_headers()
                self.end_headers()
                self.wfile.write(body)
        else:
            body = json.dumps({"error": "Endpoint not found"}).encode('utf-8')
            self.send_response(404)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(body)))
            self._send_cors_headers()
            self.end_headers()
            self.wfile.write(body)

    def log_message(self, format, *args):
        print(f"[Python-Classifier] {format % args}")

def run_server(port=PORT):
    server_address = ('0.0.0.0', port)
    httpd = ThreadingHTTPServer(server_address, IntentRequestHandler)
    print(f"============================================================")
    print(f" Campus Navigation Python Intent Classifier Service")
    print(f" Running on http://localhost:{port}")
    print(f" Endpoints: POST /classify, GET /health")
    print(f"============================================================")
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nStopping Intent Classifier Service...")
        httpd.server_close()

if __name__ == '__main__':
    run_server(PORT)
