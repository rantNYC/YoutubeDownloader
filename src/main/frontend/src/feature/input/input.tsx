import axios from "axios";
import React, {useEffect, useState} from "react";
import "./input.css";
import {FETCH_PAGE_DATA, handleError, MEDIA_URL, PROGRESS_URL} from "../../utils/Routes";
import {SearchTypes} from "../../model/SearchTypes";
import {useNavigate} from "react-router-dom";
import {useAppDispatch} from "../store/hooks";
import {populate} from "../store/media/mediaReducer";
import ProgressCircle from "../../component/spinner/progressCircle";

const Input = () => {

    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const [isLoading, setIsLoading] = useState(false);
    const [urlId, setUrlId] = useState("");
    const [searchType, setSearchType] = useState<SearchTypes>(SearchTypes.VIDEO);
    const [progress, setProgress] = useState(0);
    const [total, setTotal] = useState(0);
    const [guid, setGuid] = useState('');
    const [eventSource, setEventSource] = useState<EventSource>();

    useEffect(() => {
        const es = new EventSource(PROGRESS_URL);
        es.addEventListener("GUI_ID", (em) => {
            setGuid(em.data);
            console.log(`Guid from server: ${em.data}`);
        });

        es.onerror = () => {
            if (es.readyState === EventSource.CLOSED) {
                console.log("SSE closed (" + es.readyState + ")");
            }
            es.close();
        };

        es.onopen = () => {
            console.log("connection opened", es.readyState === EventSource.OPEN);
        };
        setEventSource(es);
    }, [])

    useEffect(() => {
        if (eventSource && total === 0 && progress === 0) {
            eventSource.addEventListener(`${guid}-progress`, (em) => {
                const result = em.data;
                if (progress !== result) {
                    setProgress(result);
                }
            });
            eventSource.addEventListener(`${guid}-total`, (em) => {
                const total = em.data;
                setTotal(total);
            });
        }
    }, [guid, progress, total, eventSource]);

    const fetchMediaData = () => {
        axios.get(FETCH_PAGE_DATA(0, 10))
            .then(res => {
                dispatch(populate(res.data));
            }).then(() => {
            navigate('/dashboard');
        }).catch(error => {
            handleError(error);
        })
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setUrlId(e.currentTarget.value);
    }

    const downloadFileServer = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoading(true);
        sendDownloadRequest();
    }

    const sendDownloadRequest = () => {
        axios.post(MEDIA_URL, {
            url: urlId,
            search: searchType
        }, {
            headers: {
                guid: guid
            }
        }).then(res => {
            dispatch(populate(res.data));
        }).then(() => {
            setIsLoading(false);
            if (eventSource) {
                eventSource.close();
            }
            navigate('/dashboard');
        }).catch(error => {
            if (eventSource) {
                eventSource.close();
            }
            setIsLoading(false);
            handleError(error);
        });
    }

    const cleanPercentage = () => {
        const percentage = total !== 0 ? ((100 * progress) / total).toFixed() : 0;
        const isNegativeOrNaN = !Number.isFinite(+percentage) || percentage < 0; // we can set non-numbers to 0 here
        const isTooHigh = percentage > 100;
        return isNegativeOrNaN ? 0 : isTooHigh ? 100 : +percentage;
    };

    return (
        <div className="Dashboard">
            <div className="card">
                <img className="circle" src="profile.png" alt="John"/>
                <button onClick={fetchMediaData}>View All Media</button>
            </div>
            <form onSubmit={downloadFileServer} className="form-search">
                <div className="radio-input">
                    <label>Playlist<input onChange={() => setSearchType(SearchTypes.PLAYLIST)}
                                          checked={searchType === SearchTypes.PLAYLIST} type="radio" value="Playlist"
                                          name="type"/></label>
                    <label>Video<input onChange={() => setSearchType(SearchTypes.VIDEO)}
                                       checked={searchType === SearchTypes.VIDEO}
                                       type="radio" value="Video"
                                       name="type"/></label>
                </div>
                <br/>
                <input className="URL-input" onChange={handleChange}
                       placeholder="Enter URL Here"/>
                <br/>
                {isLoading ?
                    <div>
                        <div className="overlay"/>
                        <ProgressCircle percentage={cleanPercentage()}/>
                    </div>
                    :
                    <button className="btn" disabled={isLoading}>Download Url</button>}
            </form>

        </div>
    );
}

export default Input;