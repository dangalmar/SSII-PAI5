import sqlite3
import datetime

ATTACK_REPLY = 'REPLY'
ATTACK_INTEGRITY = 'INTEGRITY'
ATTACK_BRUTE_FORCE = 'BRUTE_FORCE'
ATTACK_WRONG_SIGN = 'WRONG_SIGN'

def initialize_db(db):
    cur = db.cursor()
    cur.execute('CREATE TABLE IF NOT EXISTS nonces(nonce text)')
    cur.execute('CREATE TABLE IF NOT EXISTS responses(attackType text, crdate integer)')
    db.commit()

def duplicated_nonce(db, nonce):
    cur = db.cursor()
    cur.execute("SELECT count() FROM nonces n WHERE n.nonce=?", (nonce,))
    rows = cur.fetchone()[0]
    return rows>0

def insert_new_nonce(db, nonce):
    cur = db.cursor()
    cur.execute("INSERT INTO nonces VALUES (?)", (nonce,))
    db.commit()

def register_no_attack(db):
    register_attack(db,'')

def register_reply_attack(db):
    register_attack(db, ATTACK_REPLY)

def register_integrity_attack(db):
    register_attack(db, ATTACK_INTEGRITY)

def register_brute_force_attack(db):
    register_attack(db, ATTACK_BRUTE_FORCE)

def register_wrong_sign(db):
    register_attack(db, ATTACK_WRONG_SIGN)

def register_attack(db, attackType):
    cur = db.cursor()
    cur.execute("INSERT INTO responses(crdate, attackType) VALUES (?, ?)", (datetime.datetime.now().timestamp(), attackType))
    db.commit()

def select_attacked(db, attackType, since):
    cur = db.cursor()
    cur.execute("SELECT count(*) FROM responses WHERE attackType = ? AND crdate > ?", (attackType, since))
    res = cur.fetchone()[0]
    return res

def select_all_responses(db, since):
    cur = db.cursor()
    cur.execute("SELECT count(*) FROM responses WHERE crdate>?", (since,))
    res = cur.fetchone()[0]
    return res