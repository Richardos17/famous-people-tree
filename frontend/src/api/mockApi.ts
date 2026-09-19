import { EdgeType } from "@/graph/edgeType"
import { FamilyGraph } from "@/types/FamilyGraph"
import { Person } from "@/types/Person"
import { Relationship } from "@/types/Relationship"

const persons:Person[] = 
     [
        {
            wikidataId: "Q4357787",
            name: "Pauline Koch",
            birthDate: new Date("1858-02-08"),
            deathDate: new Date("1920-02-20"),
            imageLink: "https://commons.wikimedia.org/wiki/Special:FilePath/Pauline%20Koch.jpg",
            wikipediaLink: "https://en.wikipedia.org/wiki/Elsa_Einstein",
            birthCountry: "Germany"          
        },
        {
            wikidataId: "Q43577",
            name: "Pauline",
            birthDate: new Date("1858-02-08"),
            deathDate: new Date("1920-02-20"),
            imageLink: "https://commons.wikimedia.org/wiki/Special:FilePath/Pauline%20Koch.jpg",
            wikipediaLink: "https://en.wikipedia.org/wiki/Elsa_Einstein",
            birthCountry: "Germany"          
        },
        {
            wikidataId: "Q4787",
            name: "Koch",
            birthDate: new Date("1858-02-08"),
            deathDate: new Date("1920-02-20"),
            imageLink: "https://commons.wikimedia.org/wiki/Special:FilePath/Pauline%20Koch.jpg",
            wikipediaLink: "https://en.wikipedia.org/wiki/Elsa_Einstein",
            birthCountry: "Germany"          
        },
        ,
        {
            wikidataId: "Q474787",
            name: "Spouse koch",
            birthDate: new Date("1858-02-08"),
            deathDate: new Date("1920-02-20"),
            imageLink: "https://commons.wikimedia.org/wiki/Special:FilePath/Pauline%20Koch.jpg",
            wikipediaLink: "https://en.wikipedia.org/wiki/Elsa_Einstein",
            birthCountry: "Germany"          
        },
    ]
const relationships:Relationship[] =  [
        {
            fromWikidataId: "Q43577",
            toWikidataId: "Q4357787",
            type: EdgeType.Parent,

        },
        {
            fromWikidataId: "Q4787",
            toWikidataId: "Q43577",
            type: EdgeType.Parent
        },
        {
            fromWikidataId: "Q43577",
            toWikidataId: "Q474787",
            type: EdgeType.Spouse,
            marriageStartDate: new Date("1919-01-01"),
            marriageEndDate: new Date("1919-07-01"),
        },]
export function fetchMockData():FamilyGraph{
    const personMap:Map<string, Person> = new Map();
    persons.forEach(person => personMap.set(person.wikidataId, person))
    return {persons: personMap, relationships: relationships}
}        
