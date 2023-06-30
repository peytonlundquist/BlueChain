from flask import Flask 
from flask import render_template

app = Flask(__name__) 


@app.route("/")
def home():
    return render_template("index.html")

@app.route("/config")
def config():
    return render_template("config.html") 

@app.route("/live")
def live(): 
    return render_template("live.html")

if __name__ == "__main__" :
    app.run(debug=True) 