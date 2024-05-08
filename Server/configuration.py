import os
import configparser

config = configparser.ConfigParser()

config.read("configuration.cfg")

CONFIGURED_SERVER_IP = config.get('server', 'IP')
CONFIGURED_SERVER_PORT = int(config.get('server', 'PORT'))

CONFIGURED_SECRET = int(config.get('keys', 'SECRET'))
CONFIGURED_CERT = config.get('keys', 'CERT')
CONFIGURED_KEY = config.get('keys', 'KEY')

CONFIGURED_SCAN_DIRECTORY = config.get('path','SCAN_DIRECTORY')
CONFIGURED_REPORT_DIRECTORY = config.get('path','REPORT_DIRECTORY')
CONFIGURED_LOGS = config.get('path','LOGS')
CONFIGURED_HOUR_FRECUENCY = int(config.get('reports','HOUR_FRECUENCY'))

CONFIGURED_ALWAYS_CORRECT = "True" == config.get('debug', 'ALWAYS_CORRECT')
CONFIGURED_DEBUG = "True" == config.get('debug', 'DEBUG_MODE')
CONFIGURED_FAST_LOOP = "True" == config.get('debug', 'FAST_LOOP')