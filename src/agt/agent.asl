!start.

+!start : true
    <-
    .random(X);
    .wait(X*2000);
    getTopic(ID);

    //generate the argument about the topic, represented as a random array of 5 elements
    !argument(ID, []);

    !start;
    .

+!argument(ID, L) : .length(L, N) & N<5
    <-
    .random(X);

    if (X>0.5) {
        F = 1;
    } else {
        F = 0;
    }

    .concat(L, [F], L2);
    !argument(ID, L2);
    .

+!argument(ID, L)
<-  .my_name(N);
    .concat("http://rorybucd.pythonanywhere.com/argument/", ID, "/", N, URI);
    .concat("{\"argument_content\": ", L, "}",  C);
    .print("topic: ", ID, " URI: ", URI, " Content: ", C);
    postRequest(URI, C, "application/json", Code, Content);
    .print("Code: ", Code, " Content: ", Content);
    .

{ include("$jacamo/templates/common-cartago.asl") }