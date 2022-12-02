#подгрузили файл с именами модулей, библиотек и справочников
require: requirements.sc

init:
#задаем правило обработки интентов и паттернов
    # bind("selectNLUResult", function($context) {
    #     //если есть совпадение по интенту - используем интент
    #     if ($context.nluResults.intents.length > 0) {
    #         $context.nluResults.selected = $context.nluResults.intents[0];
    #         return;
    #     }
    #     //если нет совпадений по интенту - используем паттерн
    #     if ($context.nluResults.patterns.length > 0) $context.nluResults.selected = $context.nluResults.patterns[0]
    # });

#========================================= СТАРТ И ГЛОБАЛЬНЫЕ ИНТЕНТЫ ==================================================
theme: /  

    #старт диалога с интентов start и приветствия
    state: Welcome
        q!: *start                                  
        q!: * Мария *
        # intent: /Приветствие
        q: * привет *
        script: $session = {}      //обнулили переменные сессии
        #проверяем есть ли имя клиента, представляемся соотв.образом и идем в Меню или Имя
        if: $client.name
            a: Здравствуйте, {{$client.name}}. Это Мария, бот Just Tour.
            go!: /Menu/Begin
        else:
            a: Здравствуйте, я Мария, бот туристической компании Just Tour.
            go!: /Name/AskName

    #глобальный ответ на нераспознанные реплики, контекст не меняется
    state: CatchAll || noContext = true 
        event!: noMatch
        a: Простите, я не поняла. Попробуйте сказать по-другому.

    #глобальный ответ на прощание
    state: Bye || noContext = true 
        # intent!: /Прощание
        q: * до свидания *
        a: Всего доброго!

    #глобальный стейт отказа или отмены
    state: Cancel
        # intent!: /Отказ
        q!: ( * (не надо)/(не хочу)/(не буду) * )
        q!: ( (* выход* *)/(* стоп *) )
        a: Поняла, выхожу из диалога.
        go!: /Exit
    
    #глобальный стейт при выходе из сценария по желанию клиента
    state: Exit
        a: Буду на связи. Обращайтесь, если понадоблюсь!
        script: $session = {}      //обнулили переменные сессии

    #глобальный стейт при завершении сценария после отправки заявки на тур
    state: End
        a: До свидания!
        script: $session = {}      //обнулили переменные сессии


#============================================ ЗАПРОС ИМЕНИ ===============================================================

theme: /Name

    #запрашиваем имя, любые слова считаем именем, поэтому остаемся в этом стейте
    state: AskName || modal = true
        a: Как я могу к Вам обращаться?
   
        #имя совпало с переменной из списка - сохраняем его и идем в Меню
        state: GetName
            q: * $Name *
            script: $client.name = $parseTree._Name.name;
            a: {{$client.name}}, приятно познакомиться!    
            go!: /Menu/Begin
        #не хочу знакомиться - соглашаемся и идем в меню
        state: NoName
            # intent: /Отказ
            q: * нет *
            q: ( * (не надо)/(не хочу)/(не буду) * )
            a: Как вам будет удобно. Обойдемся без знакомства.
            go!: /Menu/Begin
        #выход из модального стейта при запросе погоды
        state: GoWeather
            # intent: /Погода
            q: * (~погода) *
            go!: /Weather/Begin
        #другое непонятное слово - уточняем имя это или нет
        state: GetStrangeName
            event: noMatch
            script: $session.tempName = $request.query;
            a: {{$session.tempName}}! Какое необычное имя. Вы не ошиблись? Я могу вас так называть?
            buttons:
                "Да"
                "Нет"
            #если имя - сохраняем его и идем в Меню
            state: Yes
                # intent: /Да
                q: * Да *
                script: $client.name = $session.tempName;
                a: {{$client.name}}, приятно познакомиться!
                go!: /Menu/Begin
            #если не имя - соглашаемся не знакомиться и идем в Меню
            state: No
                # intent: /Нет
                q: * Нет *
                a: Как вам будет удобно. Обойдемся без знакомства.
                go!: /Menu/Begin
                
#====================================================== МЕНЮ ===========================================================

theme: /Menu
    state: Begin
        a: Я расcказываю о погоде в разных городах мира и могу оформить заявку на подбор тура. 
        go!: /Menu/Choose

    #предложили выбор; ответ про погоду поймает входной интент Погоды
    state: Choose
        a: Что Вас интересует?
        buttons:
            "Рассказать о погоде"
            "Оформить заявку на тур"
        
        #интент Что еще умеешь - идем в начало выбора       
        state: WhatElse
            # intent: /Что еще
            q: * что еще *
            a: Я больше ничего не умею. Только рассказывать о погоде и оформлять заявку на тур.
            go!: /Menu/Choose
        #интент Отказ - идем на выход
        state: Deny
            # intent: /Отказ
            q: (* (не надо)/(ничего)/(не хочу) *)
            # intent: /Нет
            q: * нет *
            a: Как скажете.
            go!: /Exit
        #интент Прогноз погоды    
        state: Weather
            # intent: /Погода
            q: * (~погода) *
            go!: /Weather/Begin
        #интент Оформить заявку
        state: Tour
            # intent: /Тур
            q: * (~заявка) *
            q: * (~тур) *
            a: В какой город или страну хотите поехать?
            buttons:
                "Нужна консультация"
                "Еще не решил куда"
            #назван город или страна
            state: CityOrCountry
                q:  * $City * 
                q:  * $Country * 
                #запоминаем город или страну и их координаты и идем на начало Заявки
                script: getLocation ($parseTree)  
                a: Записала, {{$session.place.name}}
                go!: /Trip/Begin
            #не решил или нужна консультация - идем на начало Заявки
            state: NoSure
                # intent: /Незнаю
                q: * ~консультация *
                a: Не проблема. Заполним заявку, а менеджер поможет Вам выбрать направление
                go!: /Trip/Begin
            #все остальные ответы    
            state: NoMatch
                event: noMatch
                a: Я Вас не поняла. Давайте заполним заявку, а направление выберете потом
                go!: /Trip/Begin    
                
    #интент город/страна
    state: Location 
        # q: * $City * 
        q: * $Country * 
        #запоминаем город или страну и их координаты
        script: getLocation ($parseTree)
        #запрашиваем дату
        a: {{$session.place.name}}? Интересная идея! Сейчас узнаю какая там погода. Какую дату посмотреть?
        #дата названа - записываем дату и идем в прогноз
        state: Date
            q: * @duckling.date *
            script: $session.date = $parseTree.value;
            go!: /Weather/Begin
        #интент отказ - предлаагаем варианты и идем в меню
        state: Deny
            # intent: /Отказ
            q: ( * (не надо)/(не хочу)/(не буду) * )
            a: Хотите, оформим заявку на подбор тура? Или посмотрим погоду в другом месте.
            go!: /Menu/Choose
        #ответ непонятен - ругаемся и идем в меню
        state: NoMatch
            event: noMatch
            a: Я вас не поняла.
            go!: /Menu/Choose