import {
  PositionNode,
  firstPassPartOne,
  firstPassPartTwo,
  secondPass,
} from "@/graph/reinholdAlgo";

// tree.test.ts
describe("postOrderTraversal", () => {
  it("assigns positions", () => {
    const adjList = new Map<string, PositionNode[]>();
    adjList.set("r", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "g" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "h" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "n" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "q" },
    ]);
    adjList.set("g", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "a" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "b" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "e" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "f" },
    ]);
    adjList.set("e", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "c" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "d" },
    ]);
    adjList.set("n", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "k" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "l" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "m" },
    ]);
    adjList.set("k", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "i" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "j" },
    ]);
    adjList.set("q", [
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "o" },
      { x: null, y: null, mod: 0, shift: 0, wikidataId: "p" },
    ]);

    firstPassPartOne(adjList, "r", 0);
    console.log(adjList);
    

    const expectedAdjList = new Map<string, PositionNode[]>();
    expectedAdjList.set("r", [
      { x: 1.5, y: 0, mod: 0, shift: 0, wikidataId: "g" },
      { x: 2.5, y: 0, mod: 0, shift: 0, wikidataId: "h" },
      { x: 3.5, y: 0, mod: 2, shift: 0, wikidataId: "n" },
      { x: 4.5, y: 0, mod: 4, shift: 0, wikidataId: "q" },
    ]);
    expectedAdjList.set("g", [
      { x: 0, y: 20, mod: 0, shift: 0, wikidataId: "a" },
      { x: 1, y: 20, mod: 0, shift: 0, wikidataId: "b" },
      { x: 2, y: 20, mod: 1.5, shift: 0, wikidataId: "e" },
      { x: 3, y: 20, mod: 0, shift: 0, wikidataId: "f" },
    ]);
    expectedAdjList.set("e", [
      { x: 0, y: 40, mod: 0, shift: 0, wikidataId: "c" },
      { x: 1, y: 40, mod: 0, shift: 0, wikidataId: "d" },
    ]);
    expectedAdjList.set("n", [
      { x: 0.5, y: 20, mod: 0, shift: 0, wikidataId: "k" },
      { x: 1.5, y: 20, mod: 0, shift: 0, wikidataId: "l" },
      { x: 2.5, y: 20, mod: 0, shift: 0, wikidataId: "m" },
    ]);
    expectedAdjList.set("k", [
      { x: 0, y: 40, mod: 0, shift: 0, wikidataId: "i" },
      { x: 1, y: 40, mod: 0, shift: 0, wikidataId: "j" },
    ]);
    expectedAdjList.set("q", [
      { x: 0, y: 20, mod: 0, shift: 0, wikidataId: "o" },
      { x: 1, y: 20, mod: 0, shift: 0, wikidataId: "p" },
    ]);
    expect(adjList).toEqual(expectedAdjList);
    secondPass(adjList, "r", 0, 0);
    //console.log(adjList);
    firstPassPartTwo(adjList, "r", 0);
    console.log(adjList);
  });
});
