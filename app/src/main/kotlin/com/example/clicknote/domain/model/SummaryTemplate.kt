package com.example.clicknote.domain.model

data class SummaryTemplate(
    val id: String,
    val name: String,
    val description: String,
    val category: TemplateCategory,
    val prompt: String,
    val maxLength: Int? = null,
    val format: String? = null,
    val focusPoints: List<String> = emptyList()
)

enum class TemplateCategory {
    GENERAL,
    BUSINESS,
    ACADEMIC,
    TECHNICAL,
    CREATIVE,
    MEDICAL,
    LEGAL,
    CUSTOM
}

val defaultTemplates = listOf(
    // General Templates
    SummaryTemplate(
        id = "general_brief",
        name = "Brief Overview",
        description = "A concise summary highlighting key points",
        category = TemplateCategory.GENERAL,
        prompt = "Provide a brief overview of the main points discussed in this recording."
    ),
    SummaryTemplate(
        id = "general_detailed",
        name = "Detailed Summary",
        description = "A comprehensive summary with all important details",
        category = TemplateCategory.GENERAL,
        prompt = "Create a detailed summary covering all significant points and supporting details from this recording."
    ),
    
    // Business Templates
    SummaryTemplate(
        id = "business_meeting",
        name = "Meeting Minutes",
        description = "Structured summary of meeting discussions and action items",
        category = TemplateCategory.BUSINESS,
        prompt = "Summarize this meeting recording in a structured format including: 1. Key Discussion Points 2. Decisions Made 3. Action Items 4. Next Steps"
    ),
    SummaryTemplate(
        id = "business_strategy",
        name = "Strategic Planning",
        description = "Summary focused on strategic elements and planning",
        category = TemplateCategory.BUSINESS,
        prompt = "Extract and summarize strategic elements including: Goals, Objectives, Strategies, Tactics, and Timeline"
    ),
    
    // Academic Templates
    SummaryTemplate(
        id = "academic_lecture",
        name = "Lecture Notes",
        description = "Organized summary of academic lecture content",
        category = TemplateCategory.ACADEMIC,
        prompt = "Create structured lecture notes including: Main Concepts, Key Terms, Examples, and Important Relationships"
    ),
    SummaryTemplate(
        id = "academic_research",
        name = "Research Discussion",
        description = "Summary focused on research methodology and findings",
        category = TemplateCategory.ACADEMIC,
        prompt = "Summarize research discussion including: Methodology, Key Findings, Limitations, and Future Directions"
    ),
    
    // Technical Templates
    SummaryTemplate(
        id = "technical_specs",
        name = "Technical Specifications",
        description = "Summary of technical requirements and specifications",
        category = TemplateCategory.TECHNICAL,
        prompt = "Extract and organize technical specifications including: Requirements, Constraints, Dependencies, and Technical Details"
    ),
    SummaryTemplate(
        id = "technical_review",
        name = "Code Review",
        description = "Summary of code review discussions",
        category = TemplateCategory.TECHNICAL,
        prompt = "Summarize code review discussion including: Issues Identified, Proposed Solutions, Best Practices, and Action Items"
    ),
    
    // Creative Templates
    SummaryTemplate(
        id = "creative_brainstorm",
        name = "Brainstorming Session",
        description = "Summary of creative ideas and concepts",
        category = TemplateCategory.CREATIVE,
        prompt = "Organize brainstorming ideas into: Core Concepts, Creative Solutions, Potential Applications, and Next Steps"
    ),
    SummaryTemplate(
        id = "creative_feedback",
        name = "Design Feedback",
        description = "Summary of design review and feedback",
        category = TemplateCategory.CREATIVE,
        prompt = "Summarize design feedback including: Strengths, Areas for Improvement, Specific Suggestions, and Action Items"
    ),
    
    // Medical Templates
    SummaryTemplate(
        id = "medical_consultation",
        name = "Medical Consultation",
        description = "Structured summary of medical discussions",
        category = TemplateCategory.MEDICAL,
        prompt = "Summarize medical consultation including: Symptoms, Diagnosis, Treatment Plan, and Follow-up Actions"
    ),
    SummaryTemplate(
        id = "medical_research",
        name = "Clinical Discussion",
        description = "Summary of clinical research or case discussions",
        category = TemplateCategory.MEDICAL,
        prompt = "Extract key points from clinical discussion: Clinical Findings, Research Implications, Treatment Considerations, and Recommendations"
    ),
    
    // Legal Templates
    SummaryTemplate(
        id = "legal_case",
        name = "Case Discussion",
        description = "Summary of legal case discussions",
        category = TemplateCategory.LEGAL,
        prompt = "Summarize legal case discussion including: Key Facts, Legal Issues, Arguments, and Conclusions"
    ),
    SummaryTemplate(
        id = "legal_contract",
        name = "Contract Review",
        description = "Summary of contract review discussions",
        category = TemplateCategory.LEGAL,
        prompt = "Extract key points from contract review: Terms and Conditions, Obligations, Risks, and Recommendations"
    )
) 