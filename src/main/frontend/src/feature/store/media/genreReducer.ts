import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../store";

export interface GenreTuple {
    id: number,
    name: string
}

export interface GenresProps {
    genreTupleList: string[],
}

const emptyGenresProps: GenresProps = {genreTupleList: []}

export const genreReducer = createSlice({
    name: 'genre',
    initialState: emptyGenresProps,
    reducers: {
        genres(state, action: PayloadAction<GenreTuple[]>) {
            state.genreTupleList = action.payload.map(val => val.name);
        },
        addGenre(state, action: PayloadAction<string>){
            state.genreTupleList = [...state.genreTupleList, action.payload]
        }
    }
});

export const {genres, addGenre} = genreReducer.actions;
export const selectGenres = (state: RootState) => state.genre.genreTupleList;
export default genreReducer.reducer;