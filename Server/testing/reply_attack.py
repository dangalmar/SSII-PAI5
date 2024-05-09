import socket
import json
import time

HOST_IP = '127.0.0.1'
PORT = 7070

def send_message(message):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST_IP, PORT))
        s.sendall(message)
        response = s.recv(1024)
        return response

if __name__ == "__main__":
    message_data = {
        "message": "Hello, server!",
        "clientNumber": 0,
        "nonce": str(int(time.time() * 1000)),
        "hmac": "example_hmac",
        "messageSign": "example_signature"
    }

    json_message = json.dumps(message_data).encode('utf-8')
    
    # Enviamos la misma solicitud dos veces para simular un ataque de repetición
    for _ in range(2):
        response = send_message(json_message)
        print("Response:", response.decode('utf-8'))
        expected_response = {"RESPONSE": "Conection failed: This message have been duplicated"}
        if response == json.dumps(expected_response).encode('utf-8'):
            print("Reply attack detected! Server responded as expected.")
        else:
            print("Test failed...")
