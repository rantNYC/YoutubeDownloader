import {configureStore} from "@reduxjs/toolkit";
import mediaReducer from "./media/mediaReducer";
import genreReducer from "./media/genreReducer";

export const store = configureStore({
    reducer: {
        genre: genreReducer,
        media: mediaReducer,
    }
});

// Infer the `RootState` and `AppDispatch` types from the store itself
export type RootState = ReturnType<typeof store.getState>;
// Inferred type: {posts: PostsState, comments: CommentsState, users: UsersState}
export type AppDispatch = typeof store.dispatch;