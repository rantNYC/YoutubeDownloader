import React from 'react';
import {BrowserRouter} from "react-router-dom";
import './App.css';
import {Provider} from "react-redux";
import {store} from "./feature/store/store";
import Startup from "./feature/Startup";

function App() {
    return (
        <BrowserRouter>
            <Provider store={store}>
                <Startup/>
            </Provider>
        </BrowserRouter>
    );
}

export default App;
