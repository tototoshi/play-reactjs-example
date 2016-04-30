require('../../../node_modules/bootstrap/dist/css/bootstrap.min.css');
require('bootstrap');
require('../stylesheets/app.less');

require('es6-promise').polyfill();
require('isomorphic-fetch');

import React, { Component, PropTypes } from 'react'
import ReactDOM from 'react-dom'
import { createStore, applyMiddleware, combineReducers } from 'redux'
import { connect } from 'react-redux'
import thunkMiddleware from 'redux-thunk'
import { Provider } from 'react-redux'


// Actions
const RECEIVE_STATUSES = 'RECEIVE_STATUSES';
const TWEET = 'TWEET';
const TWEET_TEXT = 'TWEET_TEXT';
const LIKE = 'LIKE';
const RETWEET = 'RETWEET';
const SHOW_NOTIFICATION = 'SHOW_NOTIFICATION';
const HIDE_NOTIFICATION = 'HIDE_NOTIFICATION';
const SHOW_ERROR_MESSAGE = 'SHOW_ERROR_MESSAGE';
const CLEAR_ERROR_MESSAGE = 'CLEAR_ERROR_MESSAGE';

// Action Creator
function receiveStatuses(statuses) {
    return {
        type: RECEIVE_STATUSES,
        statuses: statuses
    };
}

function tweetText(text) {
    return {
        type: TWEET_TEXT,
        text: text
    }
}

function tweet(text) {
    return {
        type: TWEET,
        text: text
    };
}

function retweet(statusId) {
    return {
        type: RETWEET,
        statusId: statusId
    };
}

function like(statusId) {
    return {
        type: LIKE,
        text: statusId
    }
}

function postLike(statusId) {
    return (dispatch) => {
        const formData = new FormData();
        formData.append('id', statusId);

        const options = {
            method: 'POST',
            credentials: 'include',
            body: formData
        };

        fetch('/api/fav', options)
            .then(response => {
                if (!response.ok) {
                    return response.json()
                        .then(json => dispatch(showErrorMessage(json.message)));
                } else {
                    dispatch(like(statusId));
                    dispatch(showAndHideNotification('Like!'));
                    dispatch(clearErrorMessage());
                }
            })
    };
}

function postRetweet(statusId) {
    return (dispatch) => {
        const formData = new FormData();
        formData.append('id', statusId);

        const options = {
            method: 'POST',
            credentials: 'include',
            body: formData
        };

        fetch('/api/retweet', options)
            .then(response => {
                if (!response.ok) {
                    return response.json()
                        .then(json => dispatch(showErrorMessage(json.message)));
                } else {
                    dispatch(like(statusId));
                    dispatch(showAndHideNotification('Retweet!'));
                    dispatch(clearErrorMessage())
                }
            })
    };
}

function updateStatuses(since_id) {
    var url = '/api/timeline';
    if (since_id) {
        url += '?since_id=';
        url += encodeURIComponent(since_id);
    }

    return dispatch => {
        fetch(url, { credentials: 'include' })
            .then(response => {
                if (!response.ok) {
                    return response.json()
                        .then(json => dispatch(showErrorMessage(json.message)))
                } else {
                    return response.json()
                        .then(json => dispatch(receiveStatuses(json)))
                        .then(() => dispatch(clearErrorMessage()));
                }
            })

    }
}

function postTweet(text, sinceId) {
    return (dispatch) => {
        const formData = new FormData();
        formData.append('text', text);

        const options = {
            method: 'POST',
            credentials: 'include',
            body: formData
        };

        fetch('/api/tweet', options)
            .then(response => {
                if (!response.ok) {
                    response.json()
                        .then(json => dispatch(showErrorMessage(json.message)));

                } else {
                    dispatch(showAndHideNotification('Tweet!'));
                    dispatch(tweetText(''));
                    dispatch(updateStatuses(sinceId));
                    dispatch(clearErrorMessage());
                }
            })
    }
}

function showNotification(message) {
    return {
        type: SHOW_NOTIFICATION,
        message: message
    };
}

function hideNotification() {
    return {
        type: HIDE_NOTIFICATION
    }
}

function showAndHideNotification(message) {
    return (dispatch) => {
        dispatch(showNotification(message));
        setTimeout(() => dispatch(hideNotification()), 3000);
    }
}

function showErrorMessage(message) {
    return {
        type: SHOW_ERROR_MESSAGE,
        message: message
    }
}

function clearErrorMessage() {
    return {
        type: CLEAR_ERROR_MESSAGE
    }
}

// Reducers
const tweetTextReducer = (state = '', action) => {
    switch (action.type) {
        case TWEET_TEXT:
            return action.text;
        default:
            return state
    }
};

const statusesReducer = (state = [], action) => {
    switch (action.type) {
        case RECEIVE_STATUSES:
            return action.statuses.concat(state);
        default:
            return state;
    }
};

const errorMessageReducer = (state = '', action) => {
    switch (action.type) {
        case SHOW_ERROR_MESSAGE:
            return action.message;
        case CLEAR_ERROR_MESSAGE:
            return '';
        default:
            return state;
    }
};

const notificationReducer = (state = '', action) => {
    switch (action.type) {
        case SHOW_NOTIFICATION:
            return action.message;
        case HIDE_NOTIFICATION:
            return '';
        default:
            return state;
    }
};

// Store
const reducer = combineReducers({
    tweetText: tweetTextReducer,
    notification: notificationReducer,
    statuses: statusesReducer,
    errorMessage: errorMessageReducer
});
const initialState = {
    tweetText: '',
    notification: '',
    statuses: [],
    errorMessage: ''
};
const store = createStore(reducer, initialState, applyMiddleware(thunkMiddleware));

class App extends Component {

    constructor(props) {
        super(props);
        this.updateTimeline = this.updateTimeline.bind(this);
    }

    componentDidMount() {
        this.props.dispatch(updateStatuses());
        setInterval(() => this.updateTimeline(), 120 * 1000)
    }

    updateTimeline() {
        this.props.dispatch(updateStatuses(this.props.sinceId));
    }

    render() {
        var statuses = this.props.statuses.map(function(status) {
            return <Status key={status.id} status={status}/>;
        }.bind(this));

        var errorMessage = null;
        if (this.props.errorMessage.length > 0) {
            errorMessage = <div className="alert alert-danger">{this.props.errorMessage}</div>
        }
        return <div>
            <div className="row">
                <div className="col-md-12">
                    <TweetBox />
                    <hr />
                </div>
            </div>
            {errorMessage}
            {statuses}
            <Notification />
        </div>;
    }
}

App = connect((state) => {
    var sinceId = null;
    if (state.statuses.length > 0) {
        sinceId = state.statuses[0].id
    }
    return {
        sinceId: sinceId,
        statuses: state.statuses,
        errorMessage: state.errorMessage
    }
})(App);

class Notification extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        const message = this.props.notification;
        if (message.length > 0) {
            return <div className="row">
                <div className="col-md-3 pull-right notify alert alert-info">{message}</div>
            </div>;
        } else {
            return <div></div>;
        }
    }
}

Notification = connect((state) => {
    return {
        notification: state.notification
    }
})(Notification);

class TweetBox extends Component {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.tweet = this.tweet.bind(this);
    }

    tweet() {
        var text = ReactDOM.findDOMNode(this.refs.text).value.trim();
        this.props.dispatch(postTweet(text, this.props.sinceId));
    }

    handleChange (event) {
        this.props.dispatch(tweetText(event.target.value.substr(0, 140)));
    }

    render() {
        var text = this.props.tweetText;
        return <div>
            <textarea className="form-control" rows="4" value={text} onChange={this.handleChange} ref="text" />
            <br />
            <div className="row">
                <div className="col-md-1"><button onClick={this.tweet} className="btn btn-primary">Tweet</button></div>
                <div className="col-md-1"><span>{text.length}</span></div>
            </div>
        </div>;
    }

}

TweetBox = connect((state) => {
    var sinceId = null;
    if (state.statuses.length > 0) {
        sinceId = state.statuses[0].id
    }
    return {
        sinceId: sinceId,
        tweetText: state.tweetText
    }
})(TweetBox);

class Rt extends Component {

    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick(e) {
        e.preventDefault();
        const status = this.props.status;
        this.props.dispatch(postRetweet(status.id));
        return false;
    }

    render() {
        const status = this.props.status;
        if (status.alreadyRetweeted) {
            return <span>RT</span>;
        } else {
            return <a href="#" onClick={this.handleClick}>RT</a>;
        }
    }
}

Rt = connect()(Rt);

class Fav extends Component {

    constructor(props) {
        super(props);
        this.handleClick = this.handleClick.bind(this);
    }

    handleClick(e) {
        e.preventDefault();
        const status = this.props.status;
        this.props.dispatch(postLike(status.id));
        return false;
    }

    render() {
        const status = this.props.status;
        if (status.alreadyFavorited) {
            return <span>Fav</span>;
        } else {
            return <a href="#" onClick={this.handleClick}>Fav</a>;
        }
    }
}

Fav = connect()(Fav);

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
                            {this.props.status.retweetedStatus.photos.map(function (photo, i) {
                                return <img key={i} src={photo.url} className="media"/>;
                            })}
                            <hr />
                            <div className="row">
                                <div className="col-md-1 col-xs-1">
                                    <Rt status={this.props.status} />
                                </div>
                                <div className="col-md-1 col-xs-1">
                                    <Fav status={this.props.status} />
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
                        {this.props.status.photos.map(function (photo, i) {
                            return <img key={i} className="media" src={photo.url}/>;
                        })}
                        <hr />
                        <div className="row">
                            <div className="rt col-md-1 col-xs-1">
                                <Rt status={this.props.status} />
                            </div>
                            <div className="col-md-1 col-xs-1">
                                <Fav status={this.props.status} />
                            </div>
                        </div>
                    </div>
                </div>
                <hr/>
            </div>
        }
    }
});

class Root extends Component {
    render() {
        return (
            <Provider store={store}>
                <App />
            </Provider>
        )
    }
}

ReactDOM.render(<Root />, document.getElementById("application"));
