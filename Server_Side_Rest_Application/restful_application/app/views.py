from flask import Flask, redirect, url_for
from flask import render_template
from flask import request, session, jsonify, abort, make_response

from app import app, db
from .models import EchoTable

@app.errorhandler(400)
def not_found(error):
    '''
    400 Bad Request Error
    '''
    return make_response(jsonify( { 'error': 'Bad request' } ), 400)

@app.errorhandler(404)
def not_found(error):
    '''
    404 Not Found Error
    '''
    return make_response(jsonify( { 'error': 'Not found' } ), 404)

@app.route("/get_data", methods=['GET'])
def get_data():
    '''
    GET Request to retrieve data from database table.
    '''
    searchword = request.args.get('key', '')
    print searchword
    entry_list = get_data()
    try:
        return jsonify(entry_list), 201
    except Exception as e:
        print e

@app.route("/post_data", methods=['POST'])
def post_echo():
    '''
    POST Request to insert data into database table.
    '''
    data = request.form.get('longitude')
    print data
    try:
        # Get data in request
        longitude = request.form.get('longitude')
        latitude = request.form.get('latitude')
        message = request.form.get('echo')
        image64 = None
        print "Received Data"
        try:
            image = request.form.get('image')
            image64 = image.encode('ascii')
        except Exception as e:
            print e
        # Store data to database
        post_data(longitude, latitude, message, image64)
        return make_response(jsonify( { 'success': 'echo stored' } ), 201)
    except Exception as e:
        print "Error: " + str(e)
        return make_response(jsonify( { 'error': str(e)}), 400)



def get_data():
    '''
    Retrieve all data from table.
    '''
    entry_list = []
    entries = EchoTable.query.all()
    for e in entries:
        if e.image != None:
            (e.image).decode('ascii')
        entry_list.append({'id':e.auto_id, 'longitude':e.longitude, 'latitude':e.latitude, 'message':e.echo, 'image':e.image})
    return entry_list

def post_data(_long, _lat, _msg, _img):
    '''
    Store data into table
    '''
    entry = None
    if _img != None:
        print "No Image"
        entry = EchoTable(longitude=_long, latitude=_lat, echo=_msg, image=_img)
    else:
        print "Image Exists!"
        entry = EchoTable(longitude=_long, latitude=_lat, echo=_msg)
    db.session.add(entry)
    try:
        print "Commiting Entry"
        db.session.commit()
    except Exception as e:
        print e
        db.session.rollback()
