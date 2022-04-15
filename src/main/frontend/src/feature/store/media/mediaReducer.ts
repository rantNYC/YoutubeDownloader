import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {CollectionDownload, emptyCollectionDownload} from "../../../model/IDownload";
import {RootState} from "../store";

export const mediaSlicer = createSlice({
    name: 'media',
    initialState: emptyCollectionDownload,
    reducers: {
        populate(state, action: PayloadAction<CollectionDownload>) {
            state._embedded = action.payload._embedded;
            state._links = action.payload._links;
            state.page = action.payload.page;
        },
        remove(state, action: PayloadAction<number>) {
            state._embedded.youtubeDataInfoList = state._embedded.youtubeDataInfoList.filter(item => item.id !== action.payload)
        }
    }
})

export const {populate, remove} = mediaSlicer.actions;
export const selectMedia = (state: RootState) => state.media;
export default mediaSlicer.reducer;