require('../../../node_modules/bootstrap/dist/css/bootstrap.min.css');
require('bootstrap');
require('../stylesheets/app.less');

require('es6-promise').polyfill();
require('isomorphic-fetch');

var React = require('react');
var ReactDOM = require('react-dom');
var ReactCSSTransitionGroup = require('react-addons-css-transition-group');

var App = React.createClass({
    getInitialState: function() {
        return {
            notification: '',
            errorMessage: '',
            statuses: []
        }
    },
    componentDidMount: function() {
        fetch('/api/timeline', {
            credentials: 'include'
        }).then(function (response) {
            if (response.status >= 400) {
                throw new Error("Bad request")
            }
            return response.json();
        }).then(function (json) {
            this.setState({ statuses: json });
            // auto reload
            var updateInterval = 120 * 1000;
            setTimeout(function () {
                this.updateTimeline();
            }.bind(this), updateInterval)
        }.bind(this)).catch(function () {
            this.setState({ errorMessage: 'Network Error' });
        }.bind(this));
    },
    updateTimeline: function() {
        fetch('/api/timeline' + '?since_id=' + encodeURIComponent(this.state.statuses[0].id), {
            credentials: 'include'
        }).then(function (response) {
            if (response.status >= 400) {
                throw new Error("Bad request")
            }
            return response.json();
        }).then(function (json) {
            this.setState({ statuses: json.concat(this.state.statuses) });
        }.bind(this)).catch(function () {
            this.setState({ errorMessage: 'Network Error' });
        }.bind(this));
    },
    handleTweet: function() {
        this.notify('Tweet!')
    },
    handleFav: function () {
        this.notify('Fav!')
    },
    handleRt: function () {
        this.notify('Retweet!');
    },
    notify: function (message) {
        if (this.notifyTimer) {
            clearTimeout(this.notifyTimer);
        }
        this.setState({notification: message});
        this.notifyTimer = setTimeout(function() {
            this.setState({notification: ''})
        }.bind(this), 3000);
    },
    render: function() {
        var statuses = this.state.statuses.map(function(status) {
            return <Status onFav={this.handleFav} onRt={this.handleRt} status={status}/>;
        }.bind(this));

        var errorMessage = null;
        if (this.state.errorMessage.length > 0) {
            errorMessage = <div className="alert alert-danger">{this.state.errorMessage}</div>
        }
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <TweetBox onTweet={this.handleTweet}/>
                    <hr />
                </div>
            </div>
            {errorMessage}
            {statuses}
            <ReactCSSTransitionGroup transitionName="notification">
                <Notification message={this.state.notification}/>
            </ReactCSSTransitionGroup>
        </div>;
    }
});

var Notification = React.createClass({
    render: function () {
        if (this.props.message.length !== 0) {
            return <div className="row">
                <div className="col-md-3 pull-right notify alert alert-info">
                    {this.props.message}
                </div>
            </div>;
        } else {
            return <div></div>;
        }
    }
});

var TweetBox = React.createClass({
    getInitialState: function () {
        return { value: ''　};
    },
    tweet: function () {
        var that = this;
        var text = React.findDOMNode(this.refs.text).value.trim();
        var formData = new FormData();
        formData.append('text', text);
        fetch('/api/tweet', {
            method: 'POST',
            credentials: 'include',
            body: formData
        }).then(function (response) {
            if (response.status >= 400) {
                throw new Error("Bad request")
            }
            return response.json();
        }).then(function (json) {
            that.props.onTweet();
            that.setState({value: ''});
        }).catch(function () {
            alert('Failed to tweet');
        });
    },
    handleChange: function (event) {
        this.setState({value: event.target.value.substr(0, 140)});
    },
    render: function() {
        var value = this.state.value;
        return <div>
            <textarea className="form-control" rows="4" value={value} onChange={this.handleChange} ref="text" />
            <br />
            <button onClick={this.tweet} className="btn btn-primary">Tweet</button>
        </div>;
    }
});

var Rt = React.createClass({
    getInitialState: function () {
        return { alreadyRetweeted: this.props.alreadyRetweeted };
    },
    handleClick: function (e) {
        e.preventDefault();
        var formData = new FormData();
        formData.append('id', this.props.statusId);
        fetch('/api/retweet', {
            method: 'POST',
            credentials: 'include',
            body: formData
        }).then(function (response) {
            if (response.status >= 400) {
                throw new Error("Bad request")
            }
            this.props.onRt();
            this.setState({ alreadyRetweeted: true })
        }.bind(this)).catch(function () {
            alert('Oops!');
        });
    },
    render: function() {
        if (this.state.alreadyRetweeted) {
            return <span>RT</span>;
        } else {
            return <a href="#" onClick={this.handleClick}>RT</a>;
        }
    }
});

var Fav = React.createClass({
    getInitialState: function () {
        return { alreadyFavorited: this.props.alreadyFavorited };
    },
    handleClick: function (e) {
        e.preventDefault();
        var formData = new FormData();
        formData.append('id', this.props.statusId);
        fetch('/api/fav', {
            method: 'POST',
            credentials: 'include',
            body: formData
        }).then(function (response) {
            if (response.status >= 400) {
                console.log(response.statusText);
                throw new Error("Bad request")
            }
            this.props.onFav();
            this.setState({ alreadyFavorited: true })
        }.bind(this)).catch(function () {
            alert('Oops!');
        });
        return false;
    },
    render: function() {
        if (this.state.alreadyFavorited) {
            return <span>Fav</span>;
        } else {
            return <a href="#" onClick={this.handleClick}>Fav</a>;
        }
    }
});

var Status = React.createClass({
    render: function() {
        if (this.props.status.isRetweet) {
            return <div className="row">
                <div className="col-md-1"><img src={this.props.status.user.profileImageURL}/></div>
                <div className="col-md-11">
                    <div>@{this.props.status.id}</div>
                    <div>@{this.props.status.user.screenName}</div>
                    <div className="row well">
                        <div className="col-md-1"><img src={this.props.status.retweetedStatus.user.profileImageURL}/></div>
                        <div className="col-md-11">
                            <div>@{this.props.status.retweetedStatus.user.screenName}</div>
                            <div>{this.props.status.retweetedStatus.text}</div>
                            {this.props.status.retweetedStatus.photos.map(function (photo) {
                                return <img src={photo.url} className="media"/>;
                            })}
                            <hr />
                            <div className="row">
                                <div className="col-md-1 col-xs-1">
                                    <Rt statusId={this.props.status.retweetedStatus.id}
                                        onRt={this.props.onRt}
                                        alreadyRetweeted={this.props.status.isRetweetedByMe} />
                                </div>
                                <div className="col-md-1 col-xs-1">
                                    <Fav statusId={this.props.status.retweetedStatus.id}
                                         onFav={this.props.onFav}
                                         alreadyFavorited={this.props.status.isFavoritedByMe} />
                                </div>
                            </div>
                        </div>
                    </div>
                    <hr/>
                </div>
            </div>;
        } else {
            return <div>
                <div className="row">
                    <div className="col-md-1"><img src={this.props.status.user.profileImageURL}/></div>
                    <div className="col-md-11">
                        <div>@{this.props.status.user.screenName}</div>
                        <div>{this.props.status.text}</div>
                        {this.props.status.photos.map(function (photo) {
                            return <img className="media" src={photo.url}/>;
                        })}
                        <hr />
                        <div className="row">
                            <div className="rt col-md-1 col-xs-1">
                                <Rt statusId={this.props.status.id}
                                    onRt={this.props.onRt}
                                    alreadyRetweeted={this.props.status.isRetweetedByMe} />
                            </div>
                            <div className="col-md-1 col-xs-1">
                                <Fav statusId={this.props.status.id}
                                     onFav={this.props.onFav}
                                     alreadyFavorited={this.props.status.isFavoritedByMe} />
                            </div>
                        </div>
                    </div>
                </div>
                <hr/>
            </div>
        }
    }
});

ReactDOM.render(<App />, document.getElementById("application"));
