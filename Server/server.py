from Crypto.Signature import PKCS1_v1_5
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
from logger import warning
from db import initialize_db, duplicated_nonce, insert_new_nonce, register_no_attack, register_reply_attack, register_integrity_attack, register_brute_force_attack, select_attacked, select_all_responses, register_wrong_sign, ATTACK_INTEGRITY, ATTACK_REPLY, ATTACK_BRUTE_FORCE
from hashlib import sha256
from verify_message import verify_signature, validar_hash

import base64
import configuration
import datetime
import hmac
import json
import os
import socket
import sqlite3
import ssl


HOST_IP = configuration.CONFIGURED_SERVER_IP
PORT = configuration.CONFIGURED_SERVER_PORT

SECRET = configuration.CONFIGURED_SECRET
CERT = configuration.CONFIGURED_CERT
KEY = configuration.CONFIGURED_KEY

SCAN_DIRECTORY = configuration.CONFIGURED_SCAN_DIRECTORY
REPORT_DIRECTORY = configuration.CONFIGURED_REPORT_DIRECTORY
FRECUENCY = configuration.CONFIGURED_HOUR_FRECUENCY

DEBUG = configuration.CONFIGURED_DEBUG
ALWAYS_CORRECT = DEBUG and configuration.CONFIGURED_ALWAYS_CORRECT
FAST_LOOP = DEBUG and configuration.CONFIGURED_FAST_LOOP

ENCODING = 'utf-8'
NONCE_DB = 'nonce.db'

HOURS_UNTIL_RESET = 3
MAX_REQUESTS = 3

class Server:
    key = "602538936278945789227338510671310720268497086123762351854019088246135254642085"

    def __init__(self, host='127.0.0.1', port=7070, cert_file="./certificates/certificate1.pem",
                 key_file="./certificates/private_key1.pem"):
        self.host = host
        self.port = port
        self.cert_file = os.path.abspath(cert_file)
        self.key_file = os.path.abspath(key_file)
        self.db = sqlite3.connect(NONCE_DB)

        # UNCOMMENT FOR TLS
        # self.context = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
        # self.context.load_cert_chain(cert_file, key_file)
        # self.context.set_ciphers("ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-CHACHA20-POLY1305-SHA256:ECDHE-RSA-CHACHA20-POLY1305-SHA256:ECDHE-ECDSA-AES-256-GCM-SHA384:ECDHE-RSA-AES-256-GCM-SHA384")
        initialize_db(self.db)

    def run(self):
        print("\nServidor inicializado...\n")
        requests = 0
        last_refresh = datetime.datetime.now()
        while True:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM, 0) as sock:
                sock.bind((self.host, self.port))
                print(f'Servidor conectado al puerto {self.port}')
                sock.listen(10)

                while True:
                    
                    conn, addr = sock.accept()
                    with conn:
                        if DEBUG:
                            print(f'Nueva conexión: {addr}')
                        while True:
                            data = conn.recv(1024)

                            if not data:
                                break
                            if DEBUG:
                                print(data)

                            loaded_data = json.loads(data)

                            message = loaded_data["message"]
                            clientNumber = loaded_data["clientNumber"]
                            nonce = loaded_data["nonce"]
                            hmac = loaded_data["hmac"]
                            messageSign = loaded_data["messageSign"]
                            more_nonces = duplicated_nonce(self.db, nonce)

                            if datetime.datetime.now() >= last_refresh + datetime.timedelta(hours=HOURS_UNTIL_RESET):
                                print("Resetting database")
                                last_refresh = datetime.datetime.now()
                                requests = 0
                            
                            server_is_being_attacked = requests > MAX_REQUESTS
                            print(requests)

                            if more_nonces:
                                warning(f'VERIFICACIÓN FALLIDA: Reply attack detectado')
                                register_reply_attack(self.db)
                                response = {"RESPONSE": "Conection failed: This message have been duplicated"}
                            elif validar_hash(hmac, message, self.key, nonce) == False:
                                warning(f'VERIFICACIÓN FALLIDA: La integridad ha sido comprometida')
                                register_integrity_attack(self.db)
                                response = {"RESPONSE": "Conection failed: Message integrity have been compromised"}
                            elif server_is_being_attacked:
                                warning(f'ERROR DEL SERVIDOR: El servidor está siendo atacado, por favor intente más tarde')
                                register_brute_force_attack(self.db)
                                response = {"RESPONSE": "Conection failed: Too many requests, please try again later"}
                            elif verify_signature(messageSign, message, clientNumber) == False:
                                warning(f'ERROR DEL SERVIDOR: El servidor no puede verificar la firma del mensaje')
                                register_wrong_sign(self.db)
                                response = {"RESPONSE": "Wrong signature: Client and server sign are not the same"}
                            else:
                                insert_new_nonce(self.db, nonce)
                                register_no_attack(self.db)
                                warning(f'PETICIÓN ACEPTADA: La petición ha sido aceptada')
                                response = {"RESPONSE": " PETICION OK"}
                                requests += 1 

                            if DEBUG:
                                print("Response:", str(response))
                            dumped_response = json.dumps(response)
                            conn.sendall(bytes(dumped_response, encoding=ENCODING))

                sock.close()

    @staticmethod
    def reports():
        thread_db = sqlite3.connect(NONCE_DB)
        now = datetime.datetime.now()
        file_name = now.strftime("%d_%m_%Y")
        f = open(REPORT_DIRECTORY + '/log_' + file_name + '.txt','w')
        f.write("\n################################################\n")
        f.write("Reporte del " + now.strftime("%d %m %Y") + "\n")

        since = datetime.datetime(now.year, now.month, now.day)

        month = datetime.timedelta(days=30)
        two_months = datetime.timedelta(days=60)
        three_months = datetime.timedelta(days=90)

        # TODO Esto se puede hacer en un bucle
        p1 = since - three_months
        p2 = since - two_months
        p3 = since - month

        n_all1 = select_all_responses(thread_db, p1)
        n_reply1 = select_attacked(thread_db, ATTACK_REPLY,p1)
        n_failed_integrity1 = select_attacked(thread_db, ATTACK_INTEGRITY, p1)
        n_correct1 = n_all1 - n_reply1 - n_failed_integrity1
        n_p1 = n_correct1 / n_all1

        n_all2 = select_all_responses(thread_db, p2)
        n_reply2 = select_attacked(thread_db, ATTACK_REPLY,p2)
        n_failed_integrity2 = select_attacked(thread_db, ATTACK_INTEGRITY, p2)
        n_correct2 = n_all2 - n_reply2 - n_failed_integrity2
        n_p2 = n_correct2 / n_all2

        n_all3 = select_all_responses(thread_db, p3)
        n_reply3 = select_attacked(thread_db, ATTACK_REPLY, p3)
        n_failed_integrity3 = select_attacked(thread_db, ATTACK_INTEGRITY, p3)
        n_correct3 = n_all3 - n_reply3 - n_failed_integrity3
        n_p3 = n_correct3 / n_all3

        trend = "NO TREND"

        if (n_all1 == 0 | n_all2 == 0):
            trend = "0"
        elif (n_p3 > n_p1 & n_p3 > n_p2) | (n_p3 > n_p1 & n_p3 == n_p2) | (n_p3 == n_p1 & n_p3 > n_p2):
            trend = "+"
        elif (n_p3 < n_p1 | n_p3 < n_p2):
            trend = "-"
        elif (n_p3 == n_p1 & n_p3 == n_p2):
            trend = "0"

        print('Dia anterior')

        n_all = select_all_responses(thread_db, since)
        n_reply = select_attacked(thread_db, ATTACK_REPLY,since)
        n_failed_integrity = select_attacked(thread_db, ATTACK_INTEGRITY, since)
        suffix_msg = " messages"
        n_correct = n_all - n_reply - n_failed_integrity
        f.write("TOTAL: " + str(n_all) + suffix_msg + "\n")
        f.write("   - TREND: " + str(trend) + "\n")
        f.write("   - CORRECT: " + str(n_correct) + suffix_msg + "\n")
        f.write("   - REPLY ATTACK: " + str(n_reply) + suffix_msg + "\n")
        f.write("   - FAILED INTEGRITY: " + str(n_failed_integrity) + suffix_msg + "\n\n")

        if n_all==0:
            n_all=1
        reply_kpi = n_reply/n_all
        failed_kpi = n_failed_integrity/n_all
        correct_kpi = n_correct/n_all
        suffix_ptg = " %"
        f.write("KPI \n")
        f.write("   - TREND: " + str(trend) + "\n")
        f.write("   - CORRECT: " + str(correct_kpi) + suffix_ptg + "\n")
        f.write("   - REPLY ATTACK: " + str(reply_kpi) + suffix_ptg + "\n")
        f.write("   - FAILED INTEGRITY: " + str(failed_kpi) + suffix_ptg + "\n")
        f.write("\n--------------------------------------------------\n\n")


def generate_hmac(key, message, nonce):
    encoded_key = repr(key).encode(ENCODING)
    body = str(message) + nonce
    raw_body = body.encode(ENCODING)
    hashed = hmac.new(encoded_key, raw_body, sha256)
    return hash


if __name__ == "__main__":
    server = Server(HOST_IP, PORT, CERT, KEY)
    server.run()
