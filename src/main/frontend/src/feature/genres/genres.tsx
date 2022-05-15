import { Outlet } from "react-router-dom";
import GenreCard from "./genreCard";

interface GenresProps {
    genres: string[],
}

const Genres = ({genres}: GenresProps) => {

    return (
        <>
            <div className="container">
                {
                    genres.map(val => {
                        return (
                            <GenreCard genre={val}/>
                        )
                    })
                }
            </div>
            <Outlet />
        </>
    )
}

export default Genres;