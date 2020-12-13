import os
import secrets
from PIL import Image
from flask import render_template, url_for, flash, redirect, request
from flaskblog import app, db, bcrypt
from flaskblog.forms import *
from flaskblog.models import *
from flask_login import login_user, current_user, logout_user, login_required
import folium
from flaskblog.active_users import db1, User

@app.route("/", methods=['GET', 'POST'])
@app.route("/home", methods=['GET', 'POST'])
def home():
    form = HelpMeForm()
    form2 = VolunteerForm()
    if current_user.is_authenticated:
        if form2.validate_on_submit():
            current_user.volunteering = form2.volunteer.data
            db.session.commit()
            # return render_template('ap.html')
            return redirect(url_for('map_marker'))
        if form2.validate_on_submit():
            return redirect(url_for('helping_hand'))
        return render_template('home.html', form=form, form2=form2)
    else:
        return redirect(url_for('login'))


@app.route("/about")
def about():
    return render_template('about.html', title='About')


@app.route("/register", methods=['GET', 'POST'])
def register():
    if current_user.is_authenticated:
        return redirect(url_for('home'))
    form = RegistrationForm()
    if form.validate_on_submit():
        hashed_password = bcrypt.generate_password_hash(form.password.data).decode('utf-8')
        user = User(username=form.username.data,
                    email=form.email.data,
                    password=hashed_password,
                    address=form.address.data,
                    city=form.city.data,
                    volunteering=form.volunteer.data
                    )
        db.session.add(user)
        db.session.commit()
        flash('Your account has been created! You are now able to log in', 'success')
        return redirect(url_for('login'))
    return render_template('register.html', title='Register', form=form)


@app.route("/login", methods=['GET', 'POST'])
def login():
    if current_user.is_authenticated:
        return redirect(url_for('home'))
    form = LoginForm()
    if form.validate_on_submit():
        user = User.query.filter_by(email=form.email.data).first()
        if user and bcrypt.check_password_hash(user.password, form.password.data):
            login_user(user, remember=form.remember.data)
            next_page = request.args.get('next')
            return redirect(next_page) if next_page else redirect(url_for('home'))
        else:
            flash('Login Unsuccessful. Please check email and password', 'danger')
    return render_template('login.html', title='Login', form=form)


@app.route("/logout")
def logout():
    logout_user()
    return redirect(url_for('home'))


def save_picture(form_picture):
    random_hex = secrets.token_hex(8)
    _, f_ext = os.path.splitext(form_picture.filename)
    picture_fn = random_hex + f_ext
    picture_path = os.path.join(app.root_path, 'static/profile_pics', picture_fn)

    output_size = (125, 125)
    i = Image.open(form_picture)
    i.thumbnail(output_size)
    i.save(picture_path)

    return picture_fn


@app.route("/account", methods=['GET', 'POST'])
@login_required
def account():
    form = UpdateAccountForm()
    if form.validate_on_submit():
        if form.picture.data:
            picture_file = save_picture(form.picture.data)
            current_user.image_file = picture_file
        current_user.username = form.username.data
        current_user.email = form.email.data
        db.session.commit()
        flash('Your account has been updated!', 'success')
        return redirect(url_for('account'))
    elif request.method == 'GET':
        form.username.data = current_user.username
        form.email.data = current_user.email
    image_file = url_for('static', filename='profile_pics/' + current_user.image_file)
    return render_template('account.html', title='Account',
                           image_file=image_file, form=form)


@login_required
@app.route("/helping_hand", methods=['GET', 'POST'])
def helping_hand():
    return render_template('helping_hand.html')

def add_markers(users, map):
    for user in users:
        # tr = ''
        # tip = [each.name for each in user.neighbours

        if user.designation == 'R':
            tip = 'Rescue Team'
            icon = folium.Icon(color='blue')
        else:
            tip = 'Survivor'
            icon = folium.Icon(color='red')
        folium.Marker(
            location=user.loc_data,
            popup="<b>"+str(user.name)+"</b>",
            tooltip=tip,
            icon=icon
        ).add_to(map)
    return map

@login_required
@app.route("/map-marker")
def map_marker():
    # this map using stamen terrain
    # we add some marker here

    x = db1.child('flask_app').get()
    ids = x.val().keys()
    users = []
    i = 0
    for prime_key in ids:
        raw = x.val().get(prime_key)
        designation = raw['Designation'][0]
        lat_lng = raw['Location'].split()[1] \
            .strip('(').strip(')').split(',')
        lat_lng = list(map(float, lat_lng))
        # print(lat_lng)
        name = raw['Name']
        user = User(prime_key, i, name, designation, lat_lng)
        # print(user.prime_key, user.id_x, user.designation, user.loc_data)
        users.append(user)
        i += 1

    tile = folium.Icon(color='green')

    map_x = folium.Map(
        location=users[0].loc_data,
        tiles='Stamen Terrain',
        zoom_start=13
    )
    map_x = add_markers(users, map_x)
    # folium.Marker(
    #     location=[45.52336, -122.6750],
    #     popup="<b>Marker here</b>",
    #     tooltip="Click Here!"
    # ).add_to(map)
    #
    # folium.Marker(
    #     location=[45.55736, -122.8750],
    #     popup="<b>Marker 2 here</b>",
    #     tooltip="Click Here!",
    #     icon=folium.Icon(color='green')
    # ).add_to(map)
    #
    # folium.Marker(
    #     location=[45.53236, -122.8750],
    #     popup="<b>Marker 3 here</b>",
    #     tooltip="Click Here!",
    #     icon=folium.Icon(color='red')
    # ).add_to(map)

    return map_x._repr_html_()


