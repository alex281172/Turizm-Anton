theme: /Trip
    
    state: Begin
    #если заявку начинали оформлять то идем туда где остановились, иначе идем проверять/спрашивать имя
        if: $session.tripStep
            script: $reactions.transition ( {value:$session.tripStep, deferred: false} );
        else: 
            a: Для оформления заявки я задам вам несколько вопросов. Обязательными будут только Ваше имя и телефон
            if: $client.name
                go!: /Trip/CheckName
            else:
                go!: /Trip/AskName
    
    #если имя уже есть - проверяем актуально ли оно
    state: CheckName
        a: Ваше имя {{$client.name}}, верно?
        buttons:
            "Да"
            "Нет"
        #если да - идем на Шаг2 заявки   
        state: Yes
            intent: /Да
            a: Внесла в заявку
            go!: /Trip/Step2
        #если нет - идем спрашивать имя
        state: No
            intent: /Нет
            script: delete $client.name
            go!: /Trip/AskName
        #если отказ знакомиться - извиняемся и идем на выход
        state: Deny
            intent: /Отказ
            a: К сожалению, без имени я не могу принять заявку на тур
            go!: /Exit
        #если нет внятного ответа - возвращаемся к вопросу
        state: CatchAll || noContext = true 
            event: noMatch
            a: Пожалуйста, ответьте да или нет:
            go!: /Trip/CheckName
    
    #если имени нет - запрашиваем его
    state: AskName || modal = true
        a: Пожалуйста, назовите Ваше имя
        #имя совпало с переменной из списка - сохраняем имя, идем на Шаг2 заявки
        state: GetName
            q: * $Name *
            script: $client.name = $parseTree._Name.name;
            a: {{$client.name}}, приятно познакомиться!    
            go!: /Trip/Step2
        #не хочу знакомиться - извиняемся и идем на выход
        state: NoName
            intent: /Отказ
            a: К сожалению, без имени я не могу принять заявку на тур
            go!: /Exit
        #выход из модального стейта при запросе погоды
        state: GoWeather
            intent: /Погода
            go!: /Weather/Begin
        #другое непонятное слово - уточняем имя это или нет
        state: GetStrangeName
            event: noMatch
            script: $session.tempName = $request.query;
            a: {{$session.tempName}}! Какое необычное имя. Вы не ошиблись? Я могу вас так называть?
            buttons:
                "Да"
                "Нет"
            #если имя - сохраняем его и идем на Шаг2 заявки
            state: Yes
                intent: /Да
                script: $client.name = $session.tempName;
                a: {{$client.name}}, приятно познакомиться!
                go!: /Trip/Step2
            #если не имя - пробуем с начала
            state: No
                intent: /Нет
                q: ( (* ошиб* *)/(* не може* *) )
                a: Попробуем еще раз.
                go!: /Trip/AskName

    #если телефон есть, идем его проверять, иначе - идем его спрашивать
    state: Step2
        script: $session.tripStep = "/Trip/Step2"
        if: $client.phone
            go!: /Trip/CheckPhone
        else:
            go!: /Trip/AskPhone
        
    #проверяем актуален ли телефон
    state: CheckPhone
        a: Ваш телефон {{$client.phone}}, верно?
        buttons:
            "Да"
            "Нет"
        #если да - идем на Шаг3 заявки 
        state: Yes
            intent: /Да
            a: Внесла в заявку
            go!: /Trip/Step3
        #если нет - идем спрашивать телефон
        state: No
            intent: /Нет
            script: delete $client.phone
            go!: /Trip/AskPhone
        #если отказ - извиняемся и идем на выход
        state: Deny
            intent: /Отказ
            a: К сожалению, без номера телефона я не могу принять заявку
            go!: /Exit
        #если нет внятного ответа - возвращаемся к вопросу
        state: NoMatch || noContext = true 
            event: noMatch
            a: Пожалуйста, ответьте да или нет:
            go!: /Trip/CheckPhone
           
    #если телефона нет - запрашиваем его
    state: AskPhone
        a: Пожалуйста, назовите номер Вашего телефона
        #имя совпало с переменной из списка - сохраняем имя, идем на Шаг2 заявки
        state: GetPhone
            q: * $phone *
            script: $client.phone = $parseTree._phone
            a: {{$client.phone}} внесла в заявку
            go!: /Trip/Step3
        #не хочу знакомиться - извиняемся и идем на выход
        state: NoPhone
            intent: /Отказ
            a: К сожалению, без номера телефона я не могу принять заявку
            go!: /Exit
        #иное - ругаемся и идем на начало
        state: NoMatch
            event: noMatch
            a: Непохоже на номер телефона. Давайте попробуем еще раз.
            go!: /Trip/AskPhone
    #запрашиваем дату   
    state: Step3   
        script: $session.tripStep = "/Trip/Step3"
        a: Назовите дату начала поездки. Можно примерно
        buttons:
            "Еще не знаю"
        #введена дата - сохраняем ее
        state: Date
            q: * @duckling.date *
            script: $session.date = $parseTree.value;
            go!: /Trip/Step4
        #введено что-то иное - сохраняем это в другой переменной
        state: NoDate
            event: noMatch
            script: $session.noDate = $request.query;
            go!: /Trip/Step4
    
    #запрашиваем длительность поездки
    state: Step4 
        script: $session.tripStep = "/Trip/Step4"
        a: Выберите длительность поездки
        buttons:
            "до 7 дней"
            "7-13 дней"
            "14-20 дней"
            "21-29 дней"
            "Свыше месяца"
            "Еще не знаю"
        #подойдет любой ответ - записали его
        state: Answer
            q: *
            script: $session.duration = $request.query;
            go!: /Trip/Step5

    #запрашиваем количество участников поездки
    state: Step5 
        script: $session.tripStep = "/Trip/Step5"
        a: Сколько всего человек будет в поездке включая детей?
        buttons:
            "Еще не знаю"
        # если введено число - записали его и пошли спросить про детей
        state: Number
            q: * @duckling.number * || fromState = "/Trip/Step5", onlyThisState = true
            script: $session.people = $request.query;
            go!: /Trip/Step5/Children
        # спросили про детей 
        state: Children
            a: Сколько из них детей младше 14 лет?
            #подойдет любой ответ - записали его и идем на Шаг6 заявки
            state: Answer
                q: *
                script: $session.children = $request.query;
                go!: /Trip/Step6
        # если любой другой ответ - записали его как есть и идем на Шаг6 заявки
        state: Answer
            event: noMatch || fromState = "/Trip/Step5", onlyThisState = true
            script: $session.people = $request.query;
            go!: /Trip/Step6
            
    #запрашиваем бюджет поездки
    state: Step6 
        script: $session.tripStep = "/Trip/Step6"
        a: Какой примерно бюджет поездки из расчета на одного взрослого?
        buttons:
            "до $300"        
            "$300-$700"        
            "$700-$1500"        
            "$1500-$3000"
            "свыше $3000"
            "Еще не знаю"
        #подойдет любой ответ - записали его
        state: Answer
            q: *
            script: $session.budget = $request.query;
            go!: /Trip/Step7

    #запрашиваем бюджет поездки
    state: Step7 
        script: $session.tripStep = "/Trip/Step7"
        a: Какой хотите минимальный уровень звездности отеля?
        buttons:
            "не важно"        
            "3*"        
            "4*"        
            "5*"
            "Еще не знаю"
        #подойдет любой ответ - записали его
        state: Answer
            q: *
            script: $session.stars = $request.query;
            go!: /Trip/Step8

    #запрашиваем бюджет поездки
    state: Step8 
        script: $session.tripStep = "/Trip/Step8"
        a: Что-то еще передать менеджеру? Может, какие-то пожелания?
        #подойдет любой ответ - записали его
        state: Answer
            q: *
            script: $session.comment = $request.query;
            go!: /SendMail/Mail

#============================================= Отправка формы на почту менеджеру =============================================#            
        
theme: /SendMail
    state: Mail
        a: Заявка сформирована, отправляю в компанию Just Tour...
        #формируем и отправляем заявку
        script: $session.result = email ()
        #если результат ОК - сообщаем об этом
        if: ($session.result.status == "OK")
            a: Заявка отправлена. Менеджер Just Tour свяжется с вами в ближайшее время
            go!: /End
        #если результат не ОК - тоже сообщаем об этом и идем ловить ответ
        else:
            a: Упс. Ваша заявка не отправилась. Для подбора тура обратитесь в JustTour по телефону. Продиктовать номер?
        #если согласие диктуем и спрашиваем надо ли повторить?
        state: Phone
            intent: /Да
            q: (* ~диктовать/~продиктовать *)
            a: Телефон Just Tour 8-812-000-0000. Повторить?
            #если надо повторить - идем на шаг назад
            state: Yes
                intent: /Да
                q: (* ~повторить/~диктовать/~продиктовать *)
                go!: /SendMail/Mail/Phone
        #во всех других случаях - идем на выход прощаться
        state: NoMatch
            event: noMatch
            go!: /Exit