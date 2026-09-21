export type Person = {
    wikidataId:string
    name: string,
    birthDate: Date | null, //TODO check why dates are not being shown
    deathDate: Date | null, 
    bornIn: {
        name: string;
        wikidataId: string;
    } | null;
    imageLink: string | null, 
    wikipediaLink:string | null,
    height:number | null
}