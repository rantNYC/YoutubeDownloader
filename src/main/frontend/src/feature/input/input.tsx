import axios from "axios";
import React, {useRef, useState} from "react";
import "./input.css";
import {FETCH_PAGE_DATA, handleError, MEDIA_URL, PROGRESS_URL} from "../../utils/Routes";
import {SearchTypes} from "../../model/SearchTypes";
import {useNavigate} from "react-router-dom";
import {useAppDispatch} from "../store/hooks";
import {populate} from "../store/media/mediaReducer";
import {sliceYoutubeString} from "../../utils/StringUtils";
import ProgressCircle from "../../component/loaders/progressCircle";

const Input = () => {

    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const ref = useRef<HTMLInputElement>(null);

    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(false);    // ADDED
    const [youtubeLink, setYoutubeLink] = useState("");
    const [searchType, setSearchType] = useState<SearchTypes>(SearchTypes.PLAYLIST);
    const [progress, setProgress] = useState(0);
    const [total, setTotal] = useState(0);

    const postRequestDownload = (urlId: string, guid: string) => {
        return axios.post(MEDIA_URL, {
            url: urlId,
            search: searchType
        }, {
            headers: {
                guid: guid
            }
        })
    }

    const connectEventSource = (urlId: string) => {
        const es = new EventSource(PROGRESS_URL);
        es.addEventListener("GUI_ID", (em) => {
            const guid = em.data;
            console.log(`Guid from server: ${guid}`);
            es.addEventListener(`${guid}-progress`, (em) => {
                const result = em.data;
                if (progress !== result) {
                    setProgress(result);
                }
            });
            es.addEventListener(`${guid}-total`, (em) => {
                setTotal(em.data);
            });

            postRequestDownload(urlId, guid).then(res => {
                dispatch(populate(res.data));
                setIsLoading(false);
                if (es) {
                    es.close();
                }
                navigate('/dashboard');
            }).catch(error => {
                if (es) {
                    es.close();
                }
                setIsLoading(false);
                setError(true);
                handleError(error);
            });
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
    }

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
        const newValueIsValid = !e.target.validity.patternMismatch;
        if (error) {
            if (newValueIsValid) {
                setError(false);
            }
        }
        setYoutubeLink(e.currentTarget.value);
    }

    const validateQuery = () => {
        if (searchType === SearchTypes.PLAYLIST) {

        }
    }

    const downloadFileServer = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        setIsLoading(true);
        validateQuery();
        sendDownloadRequest();
    }

    const processYoutubeLink = (youtubeLink: string, searchType: SearchTypes): string => {
        let urlId = '';
        if (searchType === SearchTypes.PLAYLIST) {
            urlId = sliceYoutubeString(youtubeLink, 'list=', '&');
        } else if (searchType === SearchTypes.VIDEO) {
            urlId = sliceYoutubeString(youtubeLink, 'v=', '&');
        } else {
            setError(true);
            if (ref.current) ref.current.focus();
        }

        return urlId;
    }

    const sendDownloadRequest = () => {
        const urlId = processYoutubeLink(youtubeLink, searchType);
        console.log('UrlId: ' + urlId);

        connectEventSource(urlId);
    }

    const handleBlur = (e: React.FocusEvent<HTMLInputElement>) => {
        if (!error) {
            if (e.target.validity.patternMismatch) {
                if (ref.current !== null) ref.current.focus();
                setError(true);
            }
        }
    }

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
                <div>
                    <input className={error ? 'URL-invalid' : 'URL-input'} onChange={handleChange}
                           placeholder="Enter URL Here"
                           pattern="^.+youtube.+[watch\?v=|list=].+$"
                           onBlur={handleBlur}
                           ref={ref}/>
                    {error && (
                        <p role="alert" style={{color: "rgb(255, 0, 0)"}}>
                            Please enter a valid youtube url with video or playlist
                        </p>
                    )}
                </div>
                {isLoading ?
                    <div>
                        <div className="overlay"/>
                        <ProgressCircle total={total} progress={progress}/>
                    </div>
                    :
                    <button className="btn" disabled={isLoading}>Download Url</button>}
            </form>

        </div>
    );
}

export default Input;