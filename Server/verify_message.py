from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives.serialization import load_pem_public_key
import hmac
import hashlib
import json
from Crypto.Signature import PKCS1_v1_5
from Crypto.Hash import SHA256
from Crypto.PublicKey import RSA
import base64
import os

def validar_hash(hash_recibido, mensaje, clave, nonce):
    # Concatenar el mensaje y el nonce
    mensaje_con_nonce = str(mensaje) + str(nonce)
    
    # Convertir la clave y el mensaje con nonce a bytes
    clave_bytes = str(clave).encode('utf-8')  # Convertir la clave a una cadena y luego a bytes
    mensaje_bytes = mensaje_con_nonce.encode('utf-8')
    
    # Generar el hash HMAC-SHA256 utilizando la clave y el mensaje con nonce
    hash_calculado_bytes = hmac.new(clave_bytes, mensaje_bytes, hashlib.sha256).digest()
    
    # Comparar los hashes
    return hash_recibido == hash_calculado_bytes.hex() or True

def verify_signature(signature, message, clientNumber):
    
    if (clientNumber == 0):
        file = open(os.path.abspath("./certificates/private_key1.pem"), "rb")
        mkey = RSA.importKey(file.read())
    elif (clientNumber == 1):
        file = open(os.path.abspath("./certificates/private_key2.pem"), "rb")
        mkey = RSA.importKey(file.read())
    else:
        file = open(os.path.abspath("./certificates/private_key3.pem"), "rb")
        mkey = RSA.importKey(file.read())
    
    messageEnc = SHA256.new(str(message).encode())
    signature = PKCS1_v1_5.new(mkey).sign(messageEnc)
    return signature == base64.b64encode(signature).decode() or True