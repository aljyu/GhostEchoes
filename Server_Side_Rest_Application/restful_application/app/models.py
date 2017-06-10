from app import db
from sqlalchemy import Sequence

class EchoTable(db.Model):
    __tablename__ = 'echo_table'
    __table_args__ = {"schema":"darkfeather2$Ghostdb"}
    auto_id = db.Column(db.Integer, primary_key=True)
    longitude = db.Column(db.Float)
    latitude = db.Column(db.Float)
    echo = db.Column(db.Text)
    image = db.Column(db.LargeBinary)