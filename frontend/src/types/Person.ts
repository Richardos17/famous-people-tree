export type Person = {
    wikidataId:string
    name: string,
    birthDate: Date | null,
    deathDate: Date | null, 
    bornIn: {
        name: string;
        wikidataId: string;
    } | null;
    imageLink: string | null, 
    wikipediaLink:string | null,
    height:number | null
}