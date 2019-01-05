function notEmpty(id){
    if(id){
        var element = document.getElementById(id);
        if(element) {
            var input = (element.value != null) ? element.value : "";
            if (!input) {
                alert("Please enter something into the " + element.name + " field.");
                return false;
            }
            return true;
        }
    }
    return false;
}

function isInteger(id){
    if(id){
        var element = document.getElementById(id);
        if(element) {
            if(isNaN(element.value)) {
                if (element.value) {
                    alert("Non-integer value inserted into the hours or minutes field");
                    return false;
                }
                return true;
            }
            if (Number.isInteger(Number(element.value))) {
                return true;
            }
            else
                alert("Non-integer value inserted into the hours or minutes field");
        }
    }
    return false;
}