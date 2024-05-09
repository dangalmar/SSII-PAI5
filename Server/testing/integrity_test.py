import socket
import json
import time

HOST_IP = '127.0.0.1'
PORT = 7070

# Lista para almacenar los nonces generados
used_nonces = []

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

    # Enviamos la primera solicitud con el HMAC correcto
    response = send_message(json_message)
    print("Response:", response.decode('utf-8'))

    # Guardamos el nonce utilizado
    used_nonces.append(message_data["nonce"])

    # Modificamos el nonce para la segunda solicitud
    message_data["nonce"] = str(int(time.time() * 1000))
    while message_data["nonce"] in used_nonces:  # Aseguramos que el nuevo nonce no se duplique
        message_data["nonce"] = str(int(time.time() * 1000))

    # Enviamos la segunda solicitud con el nuevo nonce
    json_message = json.dumps(message_data).encode('utf-8')
    response = send_message(json_message)
    print("Response:", response.decode('utf-8'))

    expected_response = {"RESPONSE": "Conection failed: Message integrity have been compromised"}
    if response == json.dumps(expected_response).encode('utf-8'):
        print("Integrity attack detected! Server responded as expected.")
    else:
        print("Test failed...")
