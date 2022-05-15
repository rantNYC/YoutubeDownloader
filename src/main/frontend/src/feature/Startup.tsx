import React, {useEffect, useState} from "react";
import {useAppDispatch, useAppSelector} from "./store/hooks";
import axios from "axios";
import {GENRES_URL} from "../utils/Routes";
import {genres, selectGenres} from "./store/media/genreReducer";
import LoadingSpinner from "../component/loaders/spinner";
import {Route, Routes} from "react-router-dom";
import Input from "./input/input";
import Dashboard from "./dashboard/dashboard";
import Genres from "./genres/genres";
import SingleGenre from "./genres/singleGenre";

const Startup = () => {
    const dispatch = useAppDispatch();
    const [loading, setLoading] = useState(true);
    const generesCollection = useAppSelector(selectGenres);
    useEffect(() => {
        axios.get(GENRES_URL)
            .then(res => {
                if (res.data._embedded) {
                    dispatch(genres(res.data._embedded.musicGenreList));
                }
                setLoading(false);
            }).catch(err => {
            console.log(err)
        });
    }, [dispatch])


    return (
        loading ?
            <LoadingSpinner/> :
            <>
                <div className="App">
                    <Routes>
                        <Route path="/" element={<Input/>}/>
                        <Route path="/input" element={<Input/>}/>
                        <Route path="/dashboard" element={<Dashboard/>}/>
                        <Route path="/genres" element={<Genres genres={generesCollection}/>}/>
                        <Route path="/genres/:genre" element={<SingleGenre />}/>
                    </Routes>
                </div>
            </>
    );
}

export default Startup;