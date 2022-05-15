import PaginationTable from "../../component/pagination/PaginationTable";
import {useEffect} from "react";
import axios from "axios";
import {populate} from "../store/media/mediaReducer";
import {CollectionDownload, PageLinks} from "../../model/IDownload";
import {FETCH_MEDIA_BY_GENRE} from "../../utils/Routes";
import {useParams} from "react-router-dom";
import {useAppDispatch} from "../store/hooks";
import Dashboard from "../dashboard/dashboard";

type SingleGenreProps = {
    genre?: string,
}

const SingleGenre = () => {

    const dispatch = useAppDispatch();
    const { genre } : SingleGenreProps = useParams<SingleGenreProps>();
    useEffect(() => {
        if(genre) {
            axios.get(FETCH_MEDIA_BY_GENRE(genre))
                .then(res => {
                    if (res.data._embedded) {
                        dispatch(populate(res.data));
                    }
                }).catch(err => console.error("Error", err))
        }
    })

    return(
        <>
            <Dashboard/>
        </>
    )
}

export default SingleGenre;