export type PersonApi = {
    localId:string,
    wikidataId:string,
    name: string,
    birthDate: string | null, 
    deathDate: string | null, 
    bornIn: {
        name: string;
        wikidataId: string;
    }| null,
    imageLink: string | null, 
    wikipediaLink:string | null,
    height:number | null
}