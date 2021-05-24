/* Generated by Krell, do not edit by hand */

import React from 'react';
import {
    AppRegistry,
    View,
    Text
} from 'react-native';
import {name as appName} from './app.json';
import {krellUpdateRoot, onKrellReload} from './target/main.js';

let plainStyle = {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
};

/*
 * Establish a root that works for REPL based dev / prod. In the REPL case
 * the real app root will be loaded async. In prod it won't really be async
 * but we want to treat both cases the same.
 */
class KrellRoot extends React.Component {
    constructor(props) {
        super(props);
        this.state = {loaded: false, root: null, tx: 0};
    }

    render() {
        if (!this.state.loaded) {
            return (
                <View style={plainStyle}>
                    <Text>Waiting for Krell to load files.</Text>
                </View>
            );
        } else {
            return this.state.root(this.props);
        }
    }

    componentDidMount() {
        let krell = this;
        // Mounting the app the first time
        krellUpdateRoot((appRoot) => {
            let newState = Object.assign({}, krell.state);
            newState.root = appRoot;
            newState.loaded = true;
            krell.setState(newState);
        });
        // Hot reloading
        onKrellReload(() => {
            krell.setState((state, props) => {
                let newState = Object.assign({}, state);
                newState.tx++;
                return newState;
            });
        });
    }
}

AppRegistry.registerComponent(appName, () => KrellRoot);
