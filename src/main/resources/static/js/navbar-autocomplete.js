$('#artist_input').autocomplete({
    minLength: 3,
    source: function (request, response) {
        $.ajax(
            {
                type: "POST",
                contentType: "application/json",
                url: "/autocomplete",
                dataType: "json",
                data: {
                    q: request.term
                },
                success: function (data){
                    response(data.map(artist => artist.name));
                }
            }
        );
    }
});