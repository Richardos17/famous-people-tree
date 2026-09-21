import { EdgeType } from "@/graph/edgeType"

export type Relationship = {
    fromWikidataId:string,
    toWikidataId:string,
    type: EdgeType
    marriageStartDate?: Date | null,
    marriageEndDate?: Date | null
}