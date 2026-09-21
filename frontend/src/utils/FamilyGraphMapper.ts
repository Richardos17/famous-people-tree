import { EdgeType } from "@/graph/edgeType";
import { ApiResponse } from "@/types/apiTypes/ApiResponse";
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

export function mapResponseToFamilyGraph(
  apiResponse: ApiResponse,
): FamilyGraph {
  const personMap: Map<string, Person> = new Map();
  apiResponse.persons.forEach((person) => {
    personMap.set(person.wikidataId, {
      wikidataId: person.wikidataId,
      name: person.name,

      bornIn: person.bornIn,
      imageLink: person.imageLink,
      wikipediaLink: person.wikipediaLink,
      height: person.height,
      birthDate: person.birthDate ? new Date(person.birthDate) : null,
      deathDate: person.deathDate ? new Date(person.deathDate) : null,
    });
  });
  const relationships: Relationship[] = apiResponse.relationships.map(
    (relationship) => ({
      fromWikidataId: relationship.personFromWikidataId,
      toWikidataId: relationship.persontoWikidataId,
      type: mapRelationshipToEdgeType(relationship.type),
      marriageStartDate: relationship.startDate ? new Date(relationship.startDate) : null,
      marriageEndDate: relationship.endDate ? new Date(relationship.endDate) : null,
    }),
  );
  return { persons: personMap, relationships: relationships };
}
