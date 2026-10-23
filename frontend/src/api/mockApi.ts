import { EdgeType } from "@/graph/edgeType"
import { FamilyGraph } from "@/types/FamilyGraph"
import { Person } from "@/types/Person"
import { Relationship } from "@/types/Relationship"
import raw from "./mockEinstein.json";
import { ApiTreeResponse } from "@/types/apiTypes/ApiTreeResponse";
export function fetchMockData():ApiTreeResponse{
    

    return raw
}        
