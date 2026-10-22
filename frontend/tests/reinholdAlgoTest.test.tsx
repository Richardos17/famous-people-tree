import {
  PositionNode,
  firstPass,
  secondPass,
} from "@/graph/reinholdAlgo";

// tree.test.ts
describe("postOrderTraversal", () => {
  it("assigns positions", () => {
    const adjList = new Map<string, PositionNode[]>();
    adjList.set("r", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "g", finalx: 0},
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "h", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "n", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "q", finalx: 0 },
    ]);
    adjList.set("g", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "a", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "b", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "e", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "f", finalx: 0 },
    ]);
    adjList.set("e", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "c", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "d", finalx: 0 },
    ]);
    adjList.set("n", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "k", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "l", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "m", finalx: 0 },
    ]);
    adjList.set("k", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "i", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "j", finalx: 0 },
    ]);
    adjList.set("q", [
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "o", finalx: 0 },
      { x: 0, y: 0, mod: 0, shift: 0, wikidataId: "p", finalx: 0 },
    ]);

  firstPass(adjList, "r", 0, false);
    const expectedAdjList2 = new Map<string, PositionNode[]>();
    expectedAdjList2.set("r", [
      { x: 1.5, y: 0, mod: 0, shift: 0, wikidataId: "g", finalx: 1.5 },
      { x: 2.5, y: 0, mod: 0, shift: 1.5, wikidataId: "h", finalx: 4 },
      { x: 3.5, y: 0, mod: 2, shift: 3, wikidataId: "n", finalx: 6.5 },
      { x: 4.5, y: 0, mod: 4, shift: 4.5, wikidataId: "q", finalx: 9 },
    ]);
    expectedAdjList2.set("g", [
      { x: 0, y: 20, mod: 0, shift: 0, wikidataId: "a", finalx: 0 },
      { x: 1, y: 20, mod: 0, shift: 0, wikidataId: "b", finalx: 1 },
      { x: 2, y: 20, mod: 1.5, shift: 0, wikidataId: "e", finalx: 2 },
      { x: 3, y: 20, mod: 0, shift: 0, wikidataId: "f", finalx: 3 },
    ]);
    expectedAdjList2.set("e", [
      { x: 0, y: 40, mod: 0, shift: 0, wikidataId: "c", finalx: 1.5},
      { x: 1, y: 40, mod: 0, shift: 0, wikidataId: "d", finalx: 2.5 },
    ]);
    expectedAdjList2.set("n", [
      { x: 0.5, y: 20, mod: 0, shift: 0, wikidataId: "k", finalx: 5.5 },
      { x: 1.5, y: 20, mod: 0, shift: 0, wikidataId: "l", finalx: 6.5 },
      { x: 2.5, y: 20, mod: 0, shift: 0, wikidataId: "m", finalx: 7.5 },
    ]);
    expectedAdjList2.set("k", [
      { x: 0, y: 40, mod: 0, shift: 0, wikidataId: "i", finalx: 5 },
      { x: 1, y: 40, mod: 0, shift: 0, wikidataId: "j", finalx: 6 },
    ]);
    expectedAdjList2.set("q", [
      { x: 0, y: 20, mod: 0, shift: 0, wikidataId: "o", finalx: 8.5 },
      { x: 1, y: 20, mod: 0, shift: 0, wikidataId: "p", finalx: 9.5 },
    ]);
    secondPass(adjList, "r", 0, 0);
    expect(adjList).toEqual(expectedAdjList2);
    console.log(adjList)
  });
});
