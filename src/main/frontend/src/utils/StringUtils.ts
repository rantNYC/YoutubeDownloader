export const sliceYoutubeString = (link: string, firstDelimiter: string, secondDelimiter: string): string => {
    let urlId = '';
    const pos = link.indexOf(firstDelimiter);
    if (pos !== -1) {
        const sliced = link.substring(pos+firstDelimiter.length);
        const possibleAnd = sliced.indexOf(secondDelimiter);
        if (possibleAnd === -1) urlId = sliced;
        else urlId = sliced.substring(0, possibleAnd);
    }
    return urlId;
}