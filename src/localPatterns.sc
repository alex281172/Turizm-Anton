patterns:
    $phone = $regexp<((8|\+7|)[\- ]?)?\(?\d{3}\)?[\- ]?\d{1}[\- ]?\d{1}[\- ]?\d{1}[\- ]?\d{1}[\- ]?\d{1}(([\- ]?\d{1})?[\- ]?\d{1})>
    $City = $entity<cities> || converter = function(parseTree) {var id = parseTree.cities[0].value; return cities[id].value;};
    $Country = $entity<countries> || converter = function(parseTree) {var id = parseTree.countries[0].value; return countries[id].value;};
    $Name = $entity<names> || converter = function(parseTree) {var id = parseTree.names[0].value; return names[id].value;};
