package menuParser

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.*

fun printMenu() {
    val client = WebClient()
    client.options.isCssEnabled = false
    client.options.isJavaScriptEnabled = false

    val menuUrl = "https://www.zomato.com/tulsa-ok/lone-wolf-banh-mi-downtown/menu"

    val page: HtmlPage = client.getPage(menuUrl)
    val menu = page.getFirstByXPath<HtmlHeading4>("//h4[contains(text(), 'Lone Wolf Banh Mi Menu')]")

    print(parseMenu(menu))

    client.close()
}

fun parseMenu(menuTitleNode: DomNode): ZomatoMenu{
    val menu = ZomatoMenu()
    val regex = Regex("(.+) Menu")
    val result = regex.find(menuTitleNode.visibleText)
    if (result != null)
        menu.restaurant = result.groupValues[0]

    val menuNode = menuTitleNode.parentNode.childNodes[2].childNodes[0]

    val sections = mutableListOf<ZomatoSection>()
    menuNode.childNodes.forEach {
        sections.add(parseSection(it))
    }

    menu.sections = sections.toList()

    return menu
}


fun parseSection(sectionNode: DomNode): ZomatoSection{
    val section = ZomatoSection()
    section.name = sectionNode.childNodes[0].visibleText

    val count = sectionNode.childNodes.size
    val dishes = mutableListOf<ZomatoDish>()
    for(i in 2 until count)
        dishes.add(parseDish(sectionNode.childNodes[i]))

    section.dishes = dishes.toList()

    return section
}

fun parseDish(dishNode: DomNode): ZomatoDish{
    val dish = ZomatoDish()

    val text = dishNode.childNodes[0]
    val price = dishNode.childNodes[1]

    text.childNodes.forEach{
        if(it.nodeName == "h4" && it.visibleText.isNotEmpty())
            dish.name = it.visibleText
        else if(it.nodeName == "p" && it.visibleText.isNotEmpty())
            dish.description = it.visibleText
    }

    price.childNodes.forEach {
        if(it.nodeName == "p" && it.visibleText.isNotEmpty())
            dish.price = it.visibleText
    }

    return dish
}



data class ZomatoDish(var name: String? = null, var description: String? = null, var price: String? = null){
    override fun toString(): String = "name: $name, desc: $description, price: $price"
}

data class ZomatoSection(var name: String? = null, var dishes: List<ZomatoDish>? = null){
    override fun toString(): String{
        var out = "name: $name, dishes: ${dishes?.size}"
        dishes?.forEach{
            out += "\n$it"
        }
        return out
    }
}

data class ZomatoMenu(var restaurant: String? = null, var sections: List<ZomatoSection>? = null){
    override fun toString(): String {
        var out = "restaurant: $restaurant, sections: ${sections?.size}, dishes: ${sections?.sumBy { it.dishes?.size ?: 0 }}"
        sections?.forEach{
            out += "\n$it\n"
        }
        return out
    }
}
