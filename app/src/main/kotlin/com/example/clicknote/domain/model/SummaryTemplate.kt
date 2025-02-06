package com.example.clicknote.domain.model

/**
 * Default summary templates for different use cases
 */
val defaultTemplates = listOf(
    // General Templates
    SummaryTemplate(
        id = "general_brief",
        name = "Brief Overview",
        description = "A concise summary highlighting key points",
        type = TemplateType.GENERAL,
        format = SummaryFormat.BULLET_POINTS,
        style = SummaryStyle.CONCISE,
        maxLength = 500,
        options = SummaryOptions()
    ),
    SummaryTemplate(
        id = "general_detailed",
        name = "Detailed Summary",
        description = "A comprehensive summary with all important details",
        type = TemplateType.GENERAL,
        format = SummaryFormat.BULLET_POINTS,
        style = SummaryStyle.DETAILED,
        maxLength = 1000,
        options = SummaryOptions()
    ),
    
    // Business Templates
    SummaryTemplate(
        id = "business_meeting",
        name = "Meeting Minutes",
        description = "Structured summary of meeting discussions and action items",
        type = TemplateType.BUSINESS,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.BUSINESS,
        maxLength = 750,
        options = SummaryOptions(includeActionItems = true, includeTimeline = true)
    ),
    SummaryTemplate(
        id = "business_strategy",
        name = "Strategic Planning",
        description = "Summary focused on strategic elements and planning",
        type = TemplateType.BUSINESS,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.BUSINESS,
        maxLength = 1000,
        options = SummaryOptions(includeActionItems = true, includeTimeline = true)
    ),
    
    // Academic Templates
    SummaryTemplate(
        id = "academic_lecture",
        name = "Lecture Notes",
        description = "Organized summary of academic lecture content",
        type = TemplateType.ACADEMIC,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1500,
        options = SummaryOptions(includeKeyPoints = true, includeTopics = true)
    ),
    SummaryTemplate(
        id = "academic_research",
        name = "Research Discussion",
        description = "Summary focused on research methodology and findings",
        type = TemplateType.ACADEMIC,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 2000,
        options = SummaryOptions(includeKeyPoints = true, includeTopics = true)
    ),
    
    // Technical Templates
    SummaryTemplate(
        id = "technical_specs",
        name = "Technical Specifications",
        description = "Summary of technical requirements and specifications",
        type = TemplateType.TECHNICAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1000,
        options = SummaryOptions(includeKeyPoints = true, includeActionItems = true)
    ),
    SummaryTemplate(
        id = "technical_review",
        name = "Code Review",
        description = "Summary of code review discussions",
        type = TemplateType.TECHNICAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1000,
        options = SummaryOptions(includeKeyPoints = true, includeActionItems = true)
    ),
    
    // Creative Templates
    SummaryTemplate(
        id = "creative_brainstorm",
        name = "Brainstorming Session",
        description = "Summary of creative ideas and concepts",
        type = TemplateType.CREATIVE,
        format = SummaryFormat.BULLET_POINTS,
        style = SummaryStyle.CASUAL,
        maxLength = 750,
        options = SummaryOptions(includeKeyPoints = true, includeTopics = true)
    ),
    SummaryTemplate(
        id = "creative_feedback",
        name = "Design Feedback",
        description = "Summary of design review and feedback",
        type = TemplateType.CREATIVE,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.CASUAL,
        maxLength = 750,
        options = SummaryOptions(includeKeyPoints = true, includeActionItems = true)
    ),
    
    // Medical Templates
    SummaryTemplate(
        id = "medical_consultation",
        name = "Medical Consultation",
        description = "Structured summary of medical discussions",
        type = TemplateType.MEDICAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1000,
        options = SummaryOptions(includeKeyPoints = true, includeActionItems = true)
    ),
    SummaryTemplate(
        id = "medical_research",
        name = "Clinical Discussion",
        description = "Summary of clinical research or case discussions",
        type = TemplateType.MEDICAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1500,
        options = SummaryOptions(includeKeyPoints = true, includeTopics = true)
    ),
    
    // Legal Templates
    SummaryTemplate(
        id = "legal_case",
        name = "Case Discussion",
        description = "Summary of legal case discussions",
        type = TemplateType.LEGAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 2000,
        options = SummaryOptions(includeKeyPoints = true, includeTimeline = true)
    ),
    SummaryTemplate(
        id = "legal_contract",
        name = "Contract Review",
        description = "Summary of contract review discussions",
        type = TemplateType.LEGAL,
        format = SummaryFormat.OUTLINE,
        style = SummaryStyle.TECHNICAL,
        maxLength = 1500,
        options = SummaryOptions(includeKeyPoints = true, includeActionItems = true)
    )
) 