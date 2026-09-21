export type PersonApi = {
    localId:string,
    wikidataId:string,
    name: string,
    birthdate: string | null, 
    deathdate: string | null, //TODO rename symbol also in backend
    bornIn: {
        name: string;
        wikidataId: string;
    }| null,
    imageLink: string | null, 
    wikipediaLink:string | null,
    height:number | null
}