function suggest(elementId, endPoint){
    $('#' + elementId).autocomplete({
        minLength: 3,
        source: function (request, response) {
            $.ajax(
                {
                    type: "POST",
                    contentType: "application/json",
                    url: "/autocomplete/" + endPoint,
                    dataType: "json",
                    data: {
                        q : request.term
                    },
                    success: function (data){
                        response(data);
                    }
                }
            );
        }
    });
}