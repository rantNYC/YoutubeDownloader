import React, {useState} from "react";
import './updateForm.css';
import {IDownload} from "../../model/IDownload";
import {useAppSelector} from "../../feature/store/hooks";
import {selectGenres} from "../../feature/store/media/genreReducer";

interface UpdateFormProps {
    data: IDownload,
    oldTitle: string,
    oldCategory: string,

    updateForm(dataInfo: IDownload, newTitle: string, newCategory: string): void,

    setEditing(val: boolean): void;
}

const UpdateForm = ({data, oldTitle, oldCategory, updateForm, setEditing}: UpdateFormProps) => {
    const [newTitle, setNewTitle] = useState(oldTitle);
    const [newCategory, setNewCategory] = useState(oldCategory);

    const genres = useAppSelector(selectGenres);

    const handleTitle = (e: React.ChangeEvent<HTMLInputElement>) => {
        setNewTitle(e.currentTarget.value);
    }

    const handleCategory = (e: React.ChangeEvent<HTMLInputElement>) => {
        setNewCategory(e.currentTarget.value);
    }

    const submitForm = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        updateForm(data, newTitle, newCategory);
    }

    return (
        <>
            <div className="overlay"/>
            <div className='form-popup'>
                <form className="form-container" onSubmit={submitForm}>
                    <label>Update Title</label>
                    <input type="text" onInput={handleTitle}
                           value={newTitle}
                    />
                    <label>Update Category</label>
                    <input type="text" onInput={handleCategory}
                           list="genres" name="genre"/>
                    <datalist id="genres"  defaultValue={newCategory}>
                        {
                            genres.map((val, index) => {
                                return (
                                    <option key={index} value={val}/>
                                )
                            })
                        }
                    </datalist>

                    <div className="btn-container">
                        <button type="submit" className="btn">Submit</button>
                        <button type="button" className="btn" onClick={() => setEditing(false)}>Close</button>
                    </div>
                </form>
            </div>
        </>
    )
}

export default UpdateForm;