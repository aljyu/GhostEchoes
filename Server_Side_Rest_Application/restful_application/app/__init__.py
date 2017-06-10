from flask import Flask, redirect, url_for
from flask import render_template
from flask import request
from flask_sqlalchemy import SQLAlchemy


#from flask_sslify import SSLify

import re

app = Flask(__name__)
app.config.from_object('config')
#sslify = SSLify(app)
db = SQLAlchemy(app)

from app import views, models