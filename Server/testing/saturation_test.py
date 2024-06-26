import socket
import json
import time
import threading
import random

HOST_IP = '127.0.0.1'
PORT = 7070
NUM_CLIENTS = 1000

# Lista para almacenar los nonces generados
used_nonces = []

def send_message(message):
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.connect((HOST_IP, PORT))
        s.sendall(message)
        response = s.recv(1024)
        return response

def client_thread(client_id):
    furniture = {
        "camas": random.randint(1, 5),
        "mesas": random.randint(1, 10),
        "sillas": random.randint(1, 20),
        "sillones": random.randint(1, 8)
    }

    message_data = {
        "message": furniture,
        "clientNumber": client_id,
        "nonce": str(int(time.time() * 1000)),
        "hmac": "example_hmac",
        "messageSign": "example_signature"
    }

    json_message = json.dumps(message_data).encode('utf-8')

    response = send_message(json_message)
    print(f"Client {client_id} - Response:", response.decode('utf-8'))

    # Guardamos el nonce utilizado
    used_nonces.append(message_data["nonce"])

    # Modificamos el nonce para la siguiente solicitud
    message_data["nonce"] = str(int(time.time() * 1000))
    while message_data["nonce"] in used_nonces:  # Aseguramos que el nuevo nonce no se duplique
        message_data["nonce"] = str(int(time.time() * 1000))

    # Enviamos la segunda solicitud con el nuevo nonce
    json_message = json.dumps(message_data).encode('utf-8')
    response = send_message(json_message)
    print(f"Client {client_id} - Response:", response.decode('utf-8'))

    expected_response = {"RESPONSE": "Conection failed: Message integrity have been compromised"}
    if response == json.dumps(expected_response).encode('utf-8'):
        print(f"Client {client_id} - Integrity attack detected! Server responded as expected.")

if __name__ == "__main__":
    threads = []

    # Creamos y arrancamos los threads para cada cliente
    for i in range(1, NUM_CLIENTS + 1):
        thread = threading.Thread(target=client_thread, args=(i,))
        threads.append(thread)
        thread.start()

    # Esperamos a que todos los threads terminen
    for thread in threads:
        thread.join()

    print("All clients finished.")
