import React from 'react';
import {BrowserRouter, Route, Routes} from "react-router-dom";
import './App.css';
import Input from "./feature/input/input";
import Dashboard from "./feature/dashboard/dashboard";
import {Provider} from "react-redux";
import {store} from "./feature/store/store";

function App() {

    return (
        <BrowserRouter>
            <Provider store={store}>
                <div className="App">
                    <Routes>
                        <Route path="/" element={<Input/>}/>
                        <Route path="/input" element={<Input/>}/>
                        <Route path="/dashboard" element={<Dashboard/>}/>
                    </Routes>
                </div>
            </Provider>
        </BrowserRouter>
    );
}

export default App;
