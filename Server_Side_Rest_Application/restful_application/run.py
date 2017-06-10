#!flask/bin/python
from app import app, db
db.create_all()
app.run() # Removed debug and port
