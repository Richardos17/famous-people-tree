export type RelationshipApi = {
    personFromWikidataId:string,
    persontoWikidataId:string,//TODO fix this naming also in the backend
    type: string
    startDate?: string | null,
    endDate?: string | null
}