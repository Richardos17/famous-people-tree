import { EdgeType } from "@/graph/edgeType";
import { ApiPersonResponse } from "@/types/apiTypes/ApiPersonResponse";
import { ApiTreeResponse } from "@/types/apiTypes/ApiTreeResponse";
import { PersonApi } from "@/types/apiTypes/PersonApi";
import { FamilyGraph } from "@/types/FamilyGraph";
import { Person } from "@/types/Person";
import { Relationship } from "@/types/Relationship";

function mapRelationshipToEdgeType(relationshipType: string): EdgeType {
  switch (relationshipType) {
    case "parent":
      return EdgeType.Parent;
    case "spouse":
      return EdgeType.Spouse;
    default:
      throw new Error("Failed to map type of edge.");
  }
}
function mapPerson(personApi: PersonApi): Person {
  return {
    wikidataId: personApi.wikidataId,
    name: personApi.name,

    bornIn: personApi.bornIn,
    imageLink: personApi.imageLink,
    wikipediaLink: personApi.wikipediaLink,
    height: personApi.height,
    birthDate: personApi.birthdate ? new Date(personApi.birthdate) : null,
    deathDate: personApi.deathdate ? new Date(personApi.deathdate) : null,
  };
}
export function mapSearchResult(apiPersonResponse: ApiPersonResponse):Person[]{
  return apiPersonResponse.map(mapPerson);
}
export function mapResponseToFamilyGraph(
  apiResponse: ApiTreeResponse,
): FamilyGraph {
  const personMap: Map<string, Person> = new Map();
  apiResponse.persons.forEach((person) => {
    personMap.set(person.wikidataId, mapPerson(person));
  });
  const relationships: Relationship[] = apiResponse.relationships.map(
    (relationship) => ({
      fromWikidataId: relationship.personFromWikidataId,
      toWikidataId: relationship.persontoWikidataId,
      type: mapRelationshipToEdgeType(relationship.type),
      marriageStartDate: relationship.startDate
        ? new Date(relationship.startDate)
        : null,
      marriageEndDate: relationship.endDate
        ? new Date(relationship.endDate)
        : null,
    }),
  );
  return { persons: personMap, relationships: relationships };
}
