import './genre.css';
import {Link} from "react-router-dom";

interface GenreProps {
    genre: string,
}

const GenreCard = ({genre}: GenreProps) => {
    return (
        <>
            <Link to={`/genres/${genre}`}>
                <div className="card">
                    <div className="child">
                        <h2>{genre}</h2>
                    </div>
                    <div className="child"></div>
                </div>
            </Link>
        </>
    )
}

export default GenreCard;